package com.qlive.linkmicservice

import com.qlive.rtclive.QRTCProvider
import com.qlive.rtclive.QRtcLiveRoom
import com.qlive.avparam.QMixStreaming
import com.qlive.coreimpl.BaseService
import com.qlive.core.QLiveClient
import com.qlive.core.been.QExtension
import com.qlive.rtclive.MixType

internal class QAnchorHostMicHandlerImpl(private val context: MicLinkContext) : QAnchorHostMicHandler,
    BaseService() {

    private var mLinkMicMixStreamAdapter: QLinkMicMixStreamAdapter? = null
    private val mQLinkMicServiceListener = object : QLinkMicServiceListener {
        override fun onLinkerJoin(micLinker: QMicLinker) {
            context.mQRtcLiveRoom.mMixStreamManager
                .roomUser++
            if (context.mQRtcLiveRoom.mMixStreamManager.mMixType == MixType.forward) {
                val mix = mLinkMicMixStreamAdapter?.onMixStreamStart()
                context.mQRtcLiveRoom.mMixStreamManager.startMixStreamJob(mix)
            }
            val ops = mLinkMicMixStreamAdapter?.onResetMixParam(context.allLinker, micLinker, true)
            if (ops?.isEmpty() == true) {
                return
            }
            ops?.forEach {
                context.mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.put(it.uid, it)
                context.mQRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                context.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            context.mQRtcLiveRoom.mMixStreamManager.commitOpt()
        }

        override fun onLinkerLeft(micLinker: QMicLinker) {
            context.mQRtcLiveRoom.mMixStreamManager
                .roomUser--
            if (context.mQRtcLiveRoom.mMixStreamManager
                    .roomUser == 0 || context.allLinker.size == 1
            ) {
                context.mQRtcLiveRoom.mMixStreamManager.startForwardJob()
                return
            }
            val ops = mLinkMicMixStreamAdapter?.onResetMixParam(context.allLinker, micLinker, false)
            if (ops?.isEmpty() == true) {
                return
            }
            ops?.forEach {
                context.mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.put(it.uid, it)
                context.mQRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                context.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            context.mQRtcLiveRoom.mMixStreamManager.commitOpt()
        }

        override fun onLinkerMicrophoneStatusChange(micLinker: QMicLinker) {}
        override fun onLinkerCameraStatusChange(micLinker: QMicLinker) {
            val lastCamera =
                context.mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.get(micLinker.user.userId)?.cameraMergeOption
                    ?: return
            if (!lastCamera.isNeed) {
                return
            }
            if (micLinker.isOpenCamera) {
                //打开了
                context.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    micLinker.user.userId,
                    lastCamera,
                    true
                )
            } else {
                //关闭了摄像头
                context.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    micLinker.user.userId,
                    QMixStreaming.CameraMergeOption(),
                    true
                )
            }
        }

        override fun onLinkerKicked(micLinker: QMicLinker, msg: String) {
            onLinkerLeft(micLinker)
        }

        override fun onLinkerExtensionUpdate(micLinker: QMicLinker, extension: QExtension) {}
    }

    /**
     * 设置混流适配器
     * @param linkMicMixStreamAdapter
     */
    override fun setMixStreamAdapter(linkMicMixStreamAdapter: QLinkMicMixStreamAdapter?) {
        mLinkMicMixStreamAdapter = linkMicMixStreamAdapter
    }

    override fun attachRoomClient(client: QLiveClient) {
        super.attachRoomClient(client)
        val room: QRtcLiveRoom = rtcRoomGetter
        context.mQLinkMicServiceListeners.addFirst(mQLinkMicServiceListener)
        context.mQRtcLiveRoom = room
        context.mQRtcLiveRoom.addExtraQNRTCEngineEventListener(context.mExtQNClientEventListener)
    }

    override fun onDestroyed() {
        mLinkMicMixStreamAdapter = null
        super.onDestroyed()
    }

    /**
     * 获得rtc对象
     */
    private val rtcRoomGetter by lazy {
        (client as QRTCProvider).rtcRoomGetter.invoke()
    }
}