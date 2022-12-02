package com.qlive.uikituser

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qlive.chatservice.QChatRoomService
import com.qlive.chatservice.QChatRoomServiceListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.roomservice.QRoomService
import com.qlive.rtm.msg.TextMsg
import com.qlive.uikitcore.*
import com.qlive.uikitcore.ext.bg
import com.qlive.uikitcore.smartrecycler.IAdapter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//房间顶部在线用户列表
class OnlineUserView : QKitFrameLayout {
    //静态配置
    companion object {
        //单个用户头像点击事件
        var onItemUserClickListener: (context: QLiveUIKitContext?, client: QLiveClient?, view: View, user: QLiveUser) -> Unit =
            { _, _, _, _ -> }

        //列表样式适配器
        var adapterProvider: (context: QLiveUIKitContext, client: QLiveClient) -> IAdapter<QLiveUser> =
            { _, _ ->
                OnlineUserViewAdapter()
            }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val adapter: IAdapter<QLiveUser> by lazy {
        adapterProvider.invoke(kitContext!!, client!!)
    }

    //聊天室监听
    private val mChatRoomServiceListener = object :
        QChatRoomServiceListener {
        override fun onUserJoin(memberID: String) {
            refresh()
        }
        override fun onUserLeft(memberID: String) {
            refresh()
        }
        override fun onReceivedC2CMsg(msg: TextMsg) {}
        override fun onReceivedGroupMsg(msg: TextMsg) {}
        override fun onUserKicked(memberID: String) {}
        override fun onUserBeMuted(isMute: Boolean, memberID: String, duration: Long) {}
        override fun onAdminAdd(memberID: String) {}
        override fun onAdminRemoved(memberID: String, reason: String) {}
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_online
    }

    override fun initView() {
        client!!.getService(QChatRoomService::class.java)
            .addServiceListener(mChatRoomServiceListener)
        findViewById<RecyclerView>(R.id.recyOnline).layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter.bindRecycler(findViewById<RecyclerView>(R.id.recyOnline))
    }

    private var roomId = ""

    private val lazyFreshJob = Scheduler(60000) {
        refresh()
    }

    private suspend fun getOnlineUser() = suspendCoroutine<MutableList<QLiveUser>> { ct ->
        client?.getService(QRoomService::class.java)?.getOnlineUser(1, 10,
            object : QLiveCallBack<List<QLiveUser>> {
                override fun onError(code: Int, msg: String?) {
                    ct.resumeWithException(Exception(msg))
                }

                override fun onSuccess(data: List<QLiveUser>) {
                    ct.resume(ArrayList(data))
                }
            })
    }

    private var isRefreshing = false
    private var lastRefreshedTime = 0L
    private fun refresh() {
        if (isRefreshing) {
            return
        }
        if (System.currentTimeMillis() - lastRefreshedTime < 5 * 1000) {
            return
        }
        if (roomId.isEmpty()) {
            return
        }
        lastRefreshedTime = System.currentTimeMillis()
        kitContext?.lifecycleOwner?.bg {
            doWork {
                isRefreshing = true
                val users = getOnlineUser().filter {
                    it.userId != roomInfo?.anchor?.userId
                }
                adapter.setNewDataList(users as MutableList<QLiveUser>)
            }
            catchError {
                it.printStackTrace()
            }
            onFinally {
                isRefreshing = false
            }
        }
    }

    override fun onEntering(roomId: String, user: QLiveUser) {
        super.onEntering(roomId, user)
        this.roomId = roomId
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        lazyFreshJob.start()
    }

    override fun onDestroyed() {
        client!!.getService(QChatRoomService::class.java)
            .removeServiceListener(mChatRoomServiceListener)
        super.onDestroyed()
        lazyFreshJob.cancel()
    }

    override fun onLeft() {
        super.onLeft()
        roomId = ""
        adapter.setNewDataList(ArrayList<QLiveUser>())
        lazyFreshJob.cancel()
    }
}




