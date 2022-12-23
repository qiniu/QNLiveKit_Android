package com.qlive.uikitlinkmic

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.qlive.avparam.QMixStreaming
import com.qlive.avparam.QRoomConnectionState
import com.qlive.linkmicservice.QMicLinker
import com.qlive.core.*
import com.qlive.core.been.QExtension
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.liblog.QLiveLogUtil
import com.qlive.linkmicservice.*
import com.qlive.rtclive.QPushTextureView
import com.qlive.uikitcore.*
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitlinkmic.databinding.KitViewLinkersBinding

/**
 * 连麦麦位列表
 */
open class MicLinkersView : QKitViewBindingFrameMergeLayout<KitViewLinkersBinding> {

    companion object {
        /**
         * 点击事件回调 静态字段
         * @param context kit上下文
         * @param view
         * @param linker 点击了麦上哪个人
         */
        var onItemLinkerClickListener: (context: QLiveUIKitContext, client: QLiveClient, view: View, linker: QMicLinker) -> Unit =
            { _, _, _, linker ->
                Log.d("MicLinkersView", "onItemLinkerClickListener${linker.user.userId} ")
            }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val linkService get() = client!!.getService(QLinkMicService::class.java)!!

    init {
        //连麦混流参数  拉流端看到混流的位置 和上麦后麦位位置如果需要大致匹配 需要经过屏幕尺寸的换算
        //demo实现的混流方式是 混流到右上角
        /**
         * 混流每个麦位宽大小
         */
        LinkerUIHelper.mixMicWidth = 184

        /**
         * 混流每个麦位高
         */
        LinkerUIHelper.mixMicHeight = 184

        /**
         * 混流第一个麦位上间距
         */
        LinkerUIHelper.mixTopMargin = 174
        /**
         * 混流参数 每个麦位间距
         */
        LinkerUIHelper.micBottomMixMargin = 15
        /**
         * 混流参数 每个麦位右间距
         */
        LinkerUIHelper.micRightMixMargin = 30 * 3
    }

    private fun init() {
        //绑定屏幕尺寸开始换算
        LinkerUIHelper.attachUIWidth(binding.root.width, binding.root.height)
        //麦位预览窗口列表
        val rcSurfaceLp = binding.mLinkersView.layoutParams as FrameLayout.LayoutParams
        rcSurfaceLp.topMargin = LinkerUIHelper.uiTopMargin
    }

    //麦位监听
    private val mQLinkMicServiceListener = object : QLinkMicServiceListener {
        override fun onLinkerJoin(micLinker: QMicLinker) {
            Log.d("LinkerSlot", " onUserJoinLink 有人上麦 ${micLinker.user.nick}")
            binding.mLinkersView.onLinkerJoin(micLinker)
        }

        override fun onLinkerLeft(micLinker: QMicLinker) {
            binding.mLinkersView.onLinkerLeft(micLinker)
        }

        override fun onLinkerMicrophoneStatusChange(micLinker: QMicLinker) {
            binding.mLinkersView.onLinkerMicrophoneStatusChange(micLinker)
        }

        override fun onLinkerCameraStatusChange(micLinker: QMicLinker) {
            binding.mLinkersView.onLinkerCameraStatusChange(micLinker)
        }

        override fun onLinkerKicked(micLinker: QMicLinker, msg: String) {
            onLinkerLeft(micLinker)
            msg.asToast(kitContext?.androidContext)
            QLiveLogUtil.d("onLinkerKicked", "onLinkerKicked")
        }

        override fun onLinkerExtensionUpdate(micLinker: QMicLinker, extension: QExtension) {}
    }

    //观众端连麦监听
    private val mQAudienceMicHandler = object :
        QAudienceMicHandler.LinkMicHandlerListener {
        override fun onConnectionStateChanged(state: QRoomConnectionState?) {}

        /**
         * 本地角色变化
         */
        @SuppressLint("NotifyDataSetChanged")
        override fun onRoleChange(isLinker: Boolean) {
            Log.d("LinkerSlot", " lonLocalRoleChange 本地角色变化 ${isLinker}")
            binding.mLinkersView.setRole(isLinker)
            if (isLinker) {
                addAnchorPreview()
            } else {
                removeAnchorPreview()
            }
        }
    }

    //观众如若要主播离线了还要保持连麦
    private val mQLiveStatusListener = QLiveStatusListener { liveStatus, msg ->
        if (liveStatus == QLiveStatus.ANCHOR_OFFLINE
            && client!!.getService(QLinkMicService::class.java)!!.audienceMicHandler.isLinked
        ) {
            removeAnchorPreview()
        }

        if (liveStatus == QLiveStatus.ANCHOR_ONLINE
            && client!!.getService(QLinkMicService::class.java)!!.audienceMicHandler.isLinked
        ) {
            addAnchorPreview()
        }
    }

    //连麦混流适配器
    private val mLinkMicMixStreamAdapter =
        object :
            QLinkMicMixStreamAdapter {
            /**
             * 连麦开始如果要自定义混流画布和背景
             * 返回空则主播推流分辨率有多大就多大默认实现
             * @return
             */
            override fun onMixStreamStart(): QMixStreaming.MixStreamParams? {
                return null
            }

            /**
             * 混流布局适配
             * @param micLinkers 所有连麦者
             * @return 返回重设后的每个连麦者的混流布局
             */
            override fun onResetMixParam(
                micLinkers: MutableList<QMicLinker>,
                target: QMicLinker,
                isJoin: Boolean
            ): MutableList<QMixStreaming.MergeOption> {
                return LinkerUIHelper.getLinkersMixOp(micLinkers, roomInfo!!)
            }
        }

    override fun initView() {
        binding.mLinkersView.linkService = client?.getService(QLinkMicService::class.java)
        if (client!!.clientType == QClientType.PUSHER) {
            binding.mLinkersView.setRole(true)
            //我是主播
            client!!.getService(QLinkMicService::class.java).anchorHostMicHandler.setMixStreamAdapter(
                mLinkMicMixStreamAdapter
            )
        } else {
            //我是观众
            client!!.getService(QLinkMicService::class.java).audienceMicHandler.addLinkMicListener(
                mQAudienceMicHandler
            )
            client!!.addLiveStatusListener(mQLiveStatusListener)
        }
        //添加连麦麦位监听
        client!!.getService(QLinkMicService::class.java)
            .addMicLinkerListener(mQLinkMicServiceListener)
        binding.root.post {
            init()
        }
        binding.mLinkersView.onItemLinkerClickListener = { v, i ->
            onItemLinkerClickListener.invoke(kitContext!!, client!!, v, i)
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        if (isResumeUIFromFloating && client?.clientType == QClientType.PLAYER) {
            binding.root.post {
                linkService.allLinker.forEach {
                    if (it.user?.userId != roomInfo.anchor?.userId) {
                        //从销毁的activity 小窗恢复麦位置UI 没有小窗模式可以不加
                        binding.mLinkersView.onLinkerJoin(it)
                    }
                }
            }
        }
    }

    override fun onDestroyed() {
        if (client!!.clientType == QClientType.PUSHER) {
            //我是主播
            client!!.getService(QLinkMicService::class.java).anchorHostMicHandler.setMixStreamAdapter(
                null
            )
        } else {
            //我是观众
            client!!.getService(QLinkMicService::class.java).audienceMicHandler.removeLinkMicListener(
                mQAudienceMicHandler
            )
        }
        //添加连麦麦位监听
        client!!.getService(QLinkMicService::class.java)
            .removeMicLinkerListener(mQLinkMicServiceListener)

        super.onDestroyed()
    }

    private fun addAnchorPreview() {
        Log.d("LinkerSlot", "  添加窗口房主")
        binding.flAnchorSurfaceCotiner.visibility = View.VISIBLE
        binding.flAnchorSurfaceCotiner.addView(
            QPushTextureView(context).apply {
                linkService.setUserPreview(roomInfo?.anchor?.userId ?: "", this)
            },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun removeAnchorPreview() {
        Log.d("LinkerSlot", "  移除窗口房主")
        binding.flAnchorSurfaceCotiner.removeAllViews()
        binding.flAnchorSurfaceCotiner.visibility = View.INVISIBLE
    }

    override fun onLeft() {
        super.onLeft()
        binding.mLinkersView.clear()
    }
}
