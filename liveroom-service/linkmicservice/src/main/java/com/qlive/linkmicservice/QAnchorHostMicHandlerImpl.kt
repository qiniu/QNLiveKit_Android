package com.qlive.linkmicservice

import android.content.Context
import android.os.Looper
import com.qlive.rtclive.QRTCProvider
import com.qlive.rtclive.QRtcLiveRoom
import com.qlive.avparam.QMixStreaming
import com.qlive.coreimpl.BaseService
import com.qlive.core.QLiveClient
import com.qlive.core.been.QExtension
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.rtclive.MixType
import java.util.*

internal class QAnchorHostMicHandlerImpl(private val micLinkContext: MicLinkContext) :
    QAnchorHostMicHandler, BaseService() {

    private var lazyJobs = LinkedList<Runnable>()
    private var mLinkMicMixStreamAdapter: QLinkMicMixStreamAdapter? = null
    private val mQLinkMicServiceListener = object : QLinkMicServiceListener {
        override fun onLinkerJoin(micLinker: QMicLinker) {
            val actionCall = Runnable {
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.roomUser++
                if (micLinkContext.mQRtcLiveRoom.mMixStreamManager.mMixType == MixType.forward) {
                    val mix = mLinkMicMixStreamAdapter?.onMixStreamStart()
                    micLinkContext.mQRtcLiveRoom.mMixStreamManager.startMixStreamJob(mix)
                }
                val ops = mLinkMicMixStreamAdapter?.onResetMixParam(
                    micLinkContext.allLinker,
                    micLinker,
                    true
                )
                if (ops?.isEmpty() == true) {
                    return@Runnable
                }
                ops?.forEach {
                    micLinkContext.mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.put(
                        it.uid,
                        it
                    )
                    micLinkContext.mQRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                        it.uid,
                        it.microphoneMergeOption,
                        false
                    )
                    micLinkContext.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                        it.uid,
                        it.cameraMergeOption,
                        false
                    )
                }
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.commitOpt()

            }
            if (currentRoomInfo == null) {
                lazyJobs.add(actionCall)
            } else {
                actionCall.run()
            }
        }

        override fun onLinkerLeft(micLinker: QMicLinker) {
            micLinkContext.mQRtcLiveRoom.mMixStreamManager
                .roomUser--
            if (micLinkContext.mQRtcLiveRoom.mMixStreamManager
                    .roomUser == 0 || micLinkContext.allLinker.size == 1
            ) {
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.startForwardJob()
                return
            }
            val ops = mLinkMicMixStreamAdapter?.onResetMixParam(
                micLinkContext.allLinker,
                micLinker,
                false
            )
            if (ops?.isEmpty() == true) {
                return
            }
            ops?.forEach {
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.put(it.uid, it)
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            micLinkContext.mQRtcLiveRoom.mMixStreamManager.commitOpt()
        }

        override fun onLinkerMicrophoneStatusChange(micLinker: QMicLinker) {}
        override fun onLinkerCameraStatusChange(micLinker: QMicLinker) {
            val lastCamera =
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.get(micLinker.user.userId)?.cameraMergeOption
                    ?: return
            if (!lastCamera.isNeed) {
                return
            }
            if (micLinker.isOpenCamera) {
                //打开了
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    micLinker.user.userId,
                    lastCamera,
                    true
                )
            } else {
                //关闭了摄像头
                micLinkContext.mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
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

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        val room: QRtcLiveRoom = rtcRoomGetter
        micLinkContext.mQLinkMicServiceListeners.addFirst(mQLinkMicServiceListener)
        micLinkContext.mQRtcLiveRoom = room
        micLinkContext.mQRtcLiveRoom.addExtraQNRTCEngineEventListener(micLinkContext.mExtQNClientEventListener)
    }

    override fun onJoined(roomInfo: QLiveRoomInfo) {
        super.onJoined(roomInfo)
        android.os.Handler(Looper.getMainLooper()).post {
            lazyJobs.forEach {
                it.run()
            }
            lazyJobs.clear()
        }
    }

    override fun onDestroyed() {
        lazyJobs.clear()
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