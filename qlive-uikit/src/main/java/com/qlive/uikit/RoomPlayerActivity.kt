package com.qlive.uikit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.LayoutInflaterCompat
import androidx.lifecycle.lifecycleScope
import com.qlive.avparam.PreviewMode
import com.qlive.core.*
import com.qlive.avparam.RtcException
import com.qlive.core.been.QCreateRoomParam
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.liblog.QLiveLogUtil
import com.qlive.playerclient.QPlayerClient
import com.qlive.pubchatservice.QPublicChat
import com.qlive.pubchatservice.QPublicChatService
import com.qlive.qplayer.QPlayerTextureRenderView
import com.qlive.roomservice.QRoomService
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.activity.BaseFrameActivity
import com.qlive.uikitcore.ext.bg
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.qlive.sdk.QLive
import com.qlive.uikit.component.FuncCPTBeautyDialogShower
import com.qlive.uikit.component.FuncCPTPlayerFloatingHandler
import com.qlive.uikit.component.OnKeyDownMonitor
import com.qlive.uikitcore.KITLiveInflaterFactory
import com.qlive.uikitcore.getCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 观众activity
 * UI插件底座 请勿在activity 做过多UI逻辑代码
 */
class RoomPlayerActivity : BaseFrameActivity() {

    companion object {
        var replaceLayoutId = -1
        internal var startCallBack: QLiveCallBack<QLiveRoomInfo>? = null
        fun start(
            context: Context,
            roomId: String,
            extSetter: StartRoomActivityExtSetter?,
            callBack: QLiveCallBack<QLiveRoomInfo>?
        ) {

            val goRoom = {
                startCallBack = callBack
                val i = Intent(context, RoomPlayerActivity::class.java)
                i.putExtra("roomId", roomId)
                extSetter?.setExtParams(i)
                context.startActivity(i)
            }

            val clientRef = FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.clientRef
            //小窗 & activity已经销毁 ->进入不同的房间
            if (clientRef != null
                && FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.activityRef?.get() == null
                && (roomId != clientRef.getService(QRoomService::class.java).roomInfo.liveID)
            ) {
                GlobalScope.launch {
                    suspendLeftForce(clientRef, context)
                    goRoom.invoke()
                }
            } else {
                goRoom.invoke()
            }
        }

        private suspend fun suspendLeftForce(mRoomClient: QPlayerClient, context: Context) =
            suspendCoroutine<Boolean> { cont ->
                val left = {
                    mRoomClient.leaveRoom(object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            cont.resume(false)
                        }

                        override fun onSuccess(data: Void?) {
                            cont.resume(true)
                        }
                    })
                }
                //发离开房间消息
                mRoomClient.getService(QPublicChatService::class.java)
                    ?.sendByeBye(context.getString(R.string.live_bye_bye_tip),
                        object : QLiveCallBack<QPublicChat> {
                            override fun onError(code: Int, msg: String?) {
                                left.invoke()
                            }

                            override fun onSuccess(data: QPublicChat?) {
                                left.invoke()
                            }
                        })
            }
    }

    private var mRoomId = ""

    //拉流客户端
    private var mRoomClient: QPlayerClient? = null

    //离开房间函数
    private val leftRoomActionCall: (isAnchorClose: Boolean, resultCall: QLiveCallBack<Void>) -> Unit =
        { _, it ->
            mRoomClient!!.leaveRoom(object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    it.onError(code, msg)
                }

                override fun onSuccess(data: Void?) {
                    it.onSuccess(data)
                    mInflaterFactory.onLeft()
                }
            })
        }

    //加入房间函数
    private val createAndJoinRoomActionCall: (param: QCreateRoomParam?, resultCall: QLiveCallBack<Void>) -> Unit =
        { p, c ->
            Toast.makeText(this, "player activity can not create", Toast.LENGTH_SHORT).show()
        }

    private val playerRenderView: QPlayerTextureRenderView by lazy {
        findViewById(R.id.playerRenderView)
    }

    //UI组件上下文
    private val mQUIKitContext by lazy {
        QLiveUIKitContext(
            this@RoomPlayerActivity,
            supportFragmentManager,
            this@RoomPlayerActivity,
            this@RoomPlayerActivity,
            leftRoomActionCall,
            createAndJoinRoomActionCall,
            { playerRenderView },
            { null }
        )
    }

    //加入房间
    private suspend fun suspendJoinRoom(roomId: String) = suspendCoroutine<QLiveRoomInfo> { cont ->
        mRoomClient!!.joinRoom(roomId, object :
            QLiveCallBack<QLiveRoomInfo> {
            override fun onError(code: Int, msg: String) {
                cont.resumeWithException(RtcException(code, msg))
            }

            override fun onSuccess(data: QLiveRoomInfo) {
                cont.resume(data)
            }
        })
    }

    //UI组件装载器
    /**
     * UI组件以插件的形式加载进来
     * 装载器完成替换删除 功能分发操作
     */
    private val mInflaterFactory by lazy {
        KITLiveInflaterFactory(
            delegate,
            mRoomClient!!,
            mQUIKitContext
        ).apply {
            addFuncComponent(FuncCPTBeautyDialogShower(this@RoomPlayerActivity))
            addFuncComponent(FuncCPTPlayerFloatingHandler(this@RoomPlayerActivity))
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        // onCreate -> 恢复原来的room  原activity 已经销毁
        // / 或者新房间  原来的activity存在 / 不存在
        Log.d("RoomPlayerActivity", "onCreate")
        val intentRoomId = intent?.getStringExtra("roomId") ?: ""
        var isNewClient = true
        var isNewRoom = true
        val clientRef = FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.clientRef
        if (clientRef != null) {
            // 原来的activity
            //原来的悬浮窗口存在
            if (intentRoomId != clientRef.getService(QRoomService::class.java).roomInfo.liveID) {
                //销毁原来的房间
                clientRef.destroy()
                FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.dismiss()
                isNewClient = true
                //使用新的直播间
                mRoomClient = QLive.createPlayerClient()
                Log.d("RoomPlayerActivity", "onCreate跳转到新的直播间")
            } else {
                //复用原来的房间
                mRoomClient = clientRef
                FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.dismiss()
                //从悬浮窗回到当前activity
                isNewClient = false
                isNewRoom = false
                Log.d("RoomPlayerActivity", "onCreate 同ID activity")
            }
        } else {
            Log.d("RoomPlayerActivity", "onCreate原来的悬浮窗口不存在 后台没有直播间")
            //原来的悬浮窗口不存在 后台没有直播间
            isNewClient = true
            mRoomClient = QLive.createPlayerClient()
        }
        mRoomId = intentRoomId
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        LayoutInflaterCompat.setFactory2(
            LayoutInflater.from(this),
            mInflaterFactory
        )
        super.onCreate(savedInstanceState)
        initView(true, isNewClient, isNewRoom)
        playerRenderView.setDisplayAspectRatio(PreviewMode.ASPECT_RATIO_PAVED_PARENT)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // 栈顶复用
        // 1 从后台点击小窗口返回
        // 2 在直播activity 跳转到直播activity（新的）
        val intentRoomId = intent?.getStringExtra("roomId") ?: ""
        val clientRef = FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.clientRef
        FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.dismiss()
        //client 一定存在
        if (mRoomId == intentRoomId) {
            mRoomClient!!.play(playerRenderView)
            QLiveLogUtil.d("RoomPlayerActivity", "onNewIntent 旧的activity")
            initView(
                false, false, false
            )
        } else {
            mRoomId = intentRoomId
            QLiveLogUtil.d("RoomPlayerActivity", "onNewIntent 新的activity")
            lifecycleScope.launch(Dispatchers.Main) {
                suspendLeftForce(mRoomClient!!, this@RoomPlayerActivity)
                mInflaterFactory.onLeft()
                initView(false, false, true)
            }
        }
    }

    private fun initView(isNewActivity: Boolean, isNewClient: Boolean, isNewRoom: Boolean) {
        QLiveLogUtil.d("RoomPlayerActivity", "initView ${isNewClient} ${isNewRoom}")

        val joinCall = {
            mInflaterFactory.onEntering(mRoomId, QLive.getLoginUser())

            Handler(Looper.myLooper()!!).post {
                bg {
                    showLoading(true)
                    doWork {
                        //加入房间
                        val room = suspendJoinRoom(mRoomId)
                        mInflaterFactory.onGetLiveRoomInfo(room)
                        startCallBack?.onSuccess(room)
                        //开始播放
                        mRoomClient!!.play(playerRenderView)
                        //分发状态到各个UI组件
                        mInflaterFactory.onJoined(room, false)
                    }
                    catchError {
                        Toast.makeText(this@RoomPlayerActivity, it.message, Toast.LENGTH_SHORT)
                            .show()
                        startCallBack?.onError(it.getCode(), it.message)
                        finish()
                    }
                    onFinally {
                        startCallBack = null
                        showLoading(false)
                    }
                }
            }
        }
        if (!isNewActivity) {
            //旧的activity

            if (!isNewClient && !isNewRoom) {
                //没变化
                return
            }
            if (isNewRoom && !isNewClient) {
                //重新加入房间
                joinCall.invoke()
                return
            }
        } else {
            //新的activity === 新的UI
            //小窗返回原来的房间
            if (!isNewRoom && !isNewClient) {
                val room = mRoomClient?.getService(QRoomService::class.java)?.roomInfo
                if (room == null) {
                    joinCall.invoke()
                    return
                }
                mInflaterFactory.onEntering(mRoomId, QLive.getLoginUser())
                Handler(Looper.myLooper()!!).post {
                    //加入房间
                    startCallBack?.onSuccess(room)
                    mInflaterFactory.onGetLiveRoomInfo(room)
                    //分发状态到各个UI组件
                    mInflaterFactory.onJoined(room, true)
                    //开始播放
                    mRoomClient!!.play(playerRenderView)
                    startCallBack = null
                }
            } else {
                //小窗返回新的房间 / 进入新的房间
                joinCall.invoke()
            }
        }
    }

    override fun init() {}

    //安卓重写返回键事件
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        mInflaterFactory.mComponents.forEach {
            if (it is OnKeyDownMonitor) {
                if (it.onActivityKeyDown(keyCode, event)) {
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun finish() {
        QLiveLogUtil.d("RoomPlayerActivity", "RoomPlayerActivity bef")
        super.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mInflaterFactory.onDestroyed()
        //不是小窗模式
        if (FuncCPTPlayerFloatingHandler.currentFloatingPlayerView == null) {
            mRoomClient?.destroy()
            QLiveLogUtil.d("RoomPlayerActivity", "RoomPlayerActivity onDestroy 非小窗")
        } else {
            QLiveLogUtil.d("RoomPlayerActivity", "RoomPlayerActivity onDestroy  小窗")
        }
        FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.activityRef?.clear()
        mRoomClient = null
        startCallBack?.onError(-1, "")
        startCallBack = null
    }

    override fun getLayoutId(): Int {
        if (replaceLayoutId > 0) {
            return replaceLayoutId
        }
        return R.layout.kit_activity_room_player
    }

    override fun onResume() {
        super.onResume()
        if (FuncCPTPlayerFloatingHandler.currentFloatingPlayerView != null) {
            FuncCPTPlayerFloatingHandler.currentFloatingPlayerView?.dismiss()
            mRoomClient!!.play(playerRenderView)
            QLiveLogUtil.d("RoomPlayerActivity", "RoomPlayerActivity onResume  从小窗恢复")
        } else {
            QLiveLogUtil.d("RoomPlayerActivity", "RoomPlayerActivity onResume--")
        }
        mRoomClient!!.resume()
    }

    override fun onPause() {
        super.onPause()
        if (true == mQUIKitContext.getLiveFuncComponent(FuncCPTPlayerFloatingHandler::class.java)?.isGoingToRequestFloatPermission) {
            return
        }
        if (FuncCPTPlayerFloatingHandler.currentFloatingPlayerView == null) {
            mRoomClient?.pause()
        }
    }
}