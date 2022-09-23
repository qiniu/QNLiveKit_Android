package com.qlive.linkmicservice

import android.content.Context
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg
import com.qlive.jsonutil.JsonUtils
import com.qlive.coreimpl.*
import com.qlive.core.*
import com.qlive.core.been.QExtension
import com.qlive.coreimpl.BaseService
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.model.MuteMode
import com.qlive.coreimpl.model.UidExtensionMode
import com.qlive.coreimpl.model.UidMode
import com.qlive.coreimpl.model.UidMsgMode
import com.qlive.avparam.QPushRenderView
import com.qlive.rtclive.RTCRenderView

internal class QLinkMicServiceImpl : QLinkMicService, BaseService() {

    companion object {
        val liveroom_miclinker_join = "liveroom_miclinker_join"
        val liveroom_miclinker_left = "liveroom_miclinker_left"
        val liveroom_miclinker_kick = "liveroom_miclinker_kick"
        val liveroom_miclinker_microphone_mute = "liveroom_miclinker_microphone_mute"
        val liveroom_miclinker_camera_mute = "liveroom_miclinker_camera_mute"
        val liveroom_miclinker_extension_change = "liveroom_miclinker_extension_change"
    }

    private val mLinkDateSource = LinkDataSource()
    private val mMicLinkContext = MicLinkContext()
    private val mAudienceMicLinker: QAudienceMicHandlerImpl by lazy {
        QAudienceMicHandlerImpl(
            mMicLinkContext
        )
    }

    private val mAnchorHostMicLinker: QAnchorHostMicHandlerImpl by lazy {
        QAnchorHostMicHandlerImpl(
            mMicLinkContext
        )
    }
    private val mLinkMicInvitationHandler = QInvitationHandlerImpl("liveroom_linkmic_invitation")

    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            if (toID != currentRoomInfo?.chatID) {
                return false
            }
            when (msg.optAction()) {
                liveroom_miclinker_join -> {
                    if (mMicLinkContext.getMicLinker(user?.userId ?: "xasdaasda!#!@#") != null) {
                        return true
                    }
                    val micLinker =
                        JsonUtils.parseObject(msg.optData(), QMicLinker::class.java) ?: return true
                    if (mMicLinkContext.addLinker(micLinker)) {
                        mMicLinkContext.mQLinkMicServiceListeners.forEach {
                            it.onLinkerJoin(micLinker)
                        }
                    }
                }
                liveroom_miclinker_left -> {
                    if (mMicLinkContext.getMicLinker(user?.userId ?: "xasdaasda!#!@#") != null) {
                        return true
                    }
                    val micLinker =
                        JsonUtils.parseObject(msg.optData(), UidMode::class.java) ?: return true
                    mMicLinkContext.removeLinker(micLinker.uid)?.let { lincker ->
                        mMicLinkContext.mQLinkMicServiceListeners.forEach {
                            it.onLinkerLeft(lincker)
                        }
                    }

                }
                liveroom_miclinker_kick
                -> {
                    val uidMsg =
                        JsonUtils.parseObject(msg.optData(), UidMsgMode::class.java) ?: return true
                    mMicLinkContext.removeLinker(uidMsg.uid)?.let { lincker ->
                        mMicLinkContext.mQLinkMicServiceListeners.forEach {
                            it.onLinkerKicked(lincker, uidMsg.msg)
                        }
                    }
                }

                liveroom_miclinker_microphone_mute
                -> {
                    val muteMode =
                        JsonUtils.parseObject(msg.optData(), MuteMode::class.java) ?: return true
                    mMicLinkContext.getMicLinker(muteMode.uid)?.let { linker ->
                        linker.isOpenMicrophone = !muteMode.mute
                        mMicLinkContext.mQLinkMicServiceListeners.forEach {
                            it.onLinkerMicrophoneStatusChange(linker)
                        }
                    }
                }
                liveroom_miclinker_camera_mute
                -> {
                    val muteMode =
                        JsonUtils.parseObject(msg.optData(), MuteMode::class.java) ?: return true
                    mMicLinkContext.getMicLinker(muteMode.uid)?.let { linker ->
                        linker.isOpenCamera = !muteMode.mute
                        mMicLinkContext.mQLinkMicServiceListeners.forEach {
                            it.onLinkerCameraStatusChange(linker)
                        }
                    }
                }
                liveroom_miclinker_extension_change
                -> {
                    val extMode =
                        JsonUtils.parseObject(msg.optData(), UidExtensionMode::class.java)
                            ?: return true
                    mMicLinkContext.getMicLinker(extMode.uid)?.let { linker ->
                        linker.extension.put(extMode.extension.key, extMode.extension.value)
                        mMicLinkContext.mQLinkMicServiceListeners.forEach {
                            it.onLinkerExtensionUpdate(linker, extMode.extension)
                        }
                    }
                }
            }
            return false
        }
    }
    private val mRtmMsgListenerC2c = object : RtmMsgListener {
        /**
         * 收到消息
         * @return 是否继续分发
         */
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            if (liveroom_miclinker_kick == msg.optAction()) {
                val uidMsg =
                    JsonUtils.parseObject(msg.optData(), UidMsgMode::class.java) ?: return true
                mMicLinkContext.getMicLinker(uidMsg.uid)?.let { lincker ->
                    mMicLinkContext.onKickCall.invoke(lincker, uidMsg)
                }
            }
            return false
        }
    }

    /**
     * 获取当前房间所有连麦用户
     * @return
     */
    override fun getAllLinker(): MutableList<QMicLinker> {
        return mMicLinkContext.allLinker
    }

    /**
     * 设置某人的连麦视频预览
     * 麦上用户调用  上麦后才会使用切换成rtc连麦 下麦后使用拉流预览
     * @param uid
     * @param preview
     */
    override fun setUserPreview(uid: String, preview: QPushRenderView) {
        mMicLinkContext.mQRtcLiveRoom.setUserCameraWindowView(
            uid,
            (preview as RTCRenderView).getQNRender()
        )
    }

    /**
     * 踢人
     *
     * @param uid
     */
    override fun kickOutUser(uid: String, msg: String, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                val u = QLiveDataSource().searchUserByUserId(uid)
                val uidMsgMode = UidMsgMode()
                uidMsgMode.msg = msg
                uidMsgMode.uid = uid
                val rtmMsg = RtmTextMsg<UidMsgMode>(
                    liveroom_miclinker_kick,
                    uidMsgMode
                )
                RtmManager.rtmClient.sendC2cMsg(rtmMsg.toJsonString(),
                    u.imUid,
                    false,
                    object : RtmCallBack {
                        override fun onSuccess() {
                            callBack?.onSuccess(null)
                        }

                        override fun onFailure(code: Int, msg: String) {
                            callBack?.onError(code, msg)
                        }
                    })
            }
            catchError {

            }
        }
    }

    /**
     * 跟新扩展字段
     *
     * @param micLinker
     * @param extension
     */
    override fun updateExtension(
        micLinker: QMicLinker,
        extension: QExtension,
        callBack: QLiveCallBack<Void>?
    ) {
        backGround {
            doWork {
                mLinkDateSource.updateExt(micLinker, extension)
                val extMode = UidExtensionMode()
                extMode.uid = micLinker.user.userId
                extMode.extension = extension
                val rtmMsg =
                    RtmTextMsg<UidExtensionMode>(
                        liveroom_miclinker_extension_change,
                        extMode
                    )
                RtmManager.rtmClient.sendChannelMsg(
                    rtmMsg.toJsonString(),
                    currentRoomInfo?.chatID ?: "",
                    true
                )
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun addMicLinkerListener(listener: QLinkMicServiceListener) {
        mMicLinkContext.mQLinkMicServiceListeners.add(listener)
        mMicLinkContext.needSynchro = true
    }

    override fun removeMicLinkerListener(listener: QLinkMicServiceListener?) {
        mMicLinkContext.mQLinkMicServiceListeners.remove(listener)
    }

    /**
     * 获得连麦邀请处理
     *
     * @return
     */
    override fun getInvitationHandler(): QInvitationHandler {
        return mLinkMicInvitationHandler
    }

    /**
     * 观众向主播连麦
     *
     * @return
     */
    override fun getAudienceMicHandler(): QAudienceMicHandler {
        return mAudienceMicLinker
    }

    /**
     * 主播处理自己被连麦
     *
     * @return
     */
    override fun getAnchorHostMicHandler(): QAnchorHostMicHandler {
        return mAnchorHostMicLinker
    }

      override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client,appContext)
        if (client.clientType == QClientType.PUSHER) {
            mAnchorHostMicLinker.attachRoomClient(client,appContext)
        } else {
            mAudienceMicLinker.attachRoomClient(client,appContext)
        }
        mLinkMicInvitationHandler.attach()
        RtmManager.addRtmChannelListener(mRtmMsgListener)
        RtmManager.addRtmC2cListener(mRtmMsgListenerC2c)
    }

    override fun onEntering(roomId: String, user: QLiveUser) {
        super.onEntering(roomId, user)

        if (client!!.clientType == QClientType.PUSHER) {
            mAnchorHostMicLinker.onEntering(roomId, user)
        } else {
            mAudienceMicLinker.onEntering(roomId, user)
        }
        mLinkMicInvitationHandler.onEntering(roomId, user)
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        //添加一个房主麦位
        mMicLinkContext.addLinker(QMicLinker().apply {
            user = roomInfo.anchor
            userRoomID = roomInfo.liveID
            isOpenMicrophone = true
            isOpenCamera = true
        })

        if (client!!.clientType == QClientType.PUSHER) {
            mAnchorHostMicLinker.onJoined(roomInfo, isResumeUIFromFloating)
        } else {
            mAudienceMicLinker.onJoined(roomInfo, isResumeUIFromFloating)
        }

        mLinkMicInvitationHandler.onJoined(roomInfo, isResumeUIFromFloating)
    }

    override fun onLeft() {
        super.onLeft()

        if (client!!.clientType == QClientType.PUSHER) {
            mAnchorHostMicLinker.onLeft()
        } else {
            mAudienceMicLinker.onLeft()
        }

        mLinkMicInvitationHandler.onLeft()
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mMicLinkContext.mQLinkMicServiceListeners.clear()
        if (client!!.clientType == QClientType.PUSHER) {
            mAnchorHostMicLinker.onDestroyed()
        } else {
            mAudienceMicLinker.onDestroyed()
        }
        mLinkMicInvitationHandler.onDestroyed()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
        RtmManager.removeRtmC2cListener(mRtmMsgListenerC2c)
    }

    override suspend fun checkLeave() {
        if (client!!.clientType == QClientType.PUSHER) {
            mAnchorHostMicLinker.checkLeave()
        } else {
            mAudienceMicLinker.checkLeave()
        }
    }

}