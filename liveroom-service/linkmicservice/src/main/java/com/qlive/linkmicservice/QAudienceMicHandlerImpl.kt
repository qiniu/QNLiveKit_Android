package com.qlive.linkmicservice

import com.qlive.avparam.QPlayerProvider
import com.qlive.rtm.RtmException
import com.qlive.rtm.RtmManager
import com.qlive.rtm.msg.RtmTextMsg
import com.qlive.rtm.sendChannelMsg
import com.qlive.rtclive.*
import com.qiniu.droid.rtc.*
import com.qlive.jsonutil.JsonUtils
import com.qlive.avparam.*
import com.qlive.linkmicservice.QLinkMicServiceImpl.Companion.liveroom_miclinker_camera_mute
import com.qlive.linkmicservice.QLinkMicServiceImpl.Companion.liveroom_miclinker_join
import com.qlive.linkmicservice.QLinkMicServiceImpl.Companion.liveroom_miclinker_left
import com.qlive.linkmicservice.QLinkMicServiceImpl.Companion.liveroom_miclinker_microphone_mute
import com.qlive.coreimpl.*
import com.qlive.core.*
import com.qlive.coreimpl.BaseService
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.coreimpl.model.MuteMode
import com.qlive.coreimpl.model.UidMode
import com.qlive.coreimpl.model.UidMsgMode
import com.qlive.coreimpl.util.backGround
import com.qlive.coreimpl.util.getCode
import com.qlive.liblog.QLiveLogUtil
import com.qlive.linkmicservice.QLinkMicServiceImpl.Companion.liveroom_miclinker_kick
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class QAudienceMicHandlerImpl(val context: MicLinkContext) : QAudienceMicHandler, BaseService() {

    init {
        context.hostLeftCall = {
            if (isLinked()) {
                QLiveLogUtil.d("房主下麦")
                stopLink(null)
            }
        }
    }

    private val mLinkDateSource = LinkDataSource()
    private var mPlayer: QIPlayer? = null
    private val mLinkMicHandlerListeners = ArrayList<QAudienceMicHandler.LinkMicHandlerListener>()
    private val mMeLinker: QMicLinker?
        get() {
            return context.getMicLinker(user?.userId ?: "hjhb")
        }

    private fun compare(old: List<QMicLinker>, new: List<QMicLinker>): Boolean {
        if (old.size - new.size != 1) {
            return false
        }
        var oldKey = ""
        old.forEach {
            if (it.user.userId != currentRoomInfo?.anchor?.userId) {
                oldKey += it.user.userId
            }
        }
        var newKey = ""
        new.forEach {
            if (it.user.userId != currentRoomInfo?.anchor?.userId) {
                newKey += it.user.userId
            }
        }
        return newKey == oldKey
    }

    //麦位同步
    private val mMicListJob = com.qlive.coreimpl.Scheduler(10000) {
        if (currentRoomInfo == null) {
            return@Scheduler
        }
        if (isLinked()) {
            return@Scheduler
        }
        //没有人注册监听不同步麦位
        if (!context.needSynchro) {
            return@Scheduler
        }
        backGround {
            doWork {
                val list = mLinkDateSource.getMicList(currentRoomInfo?.liveID ?: "")
                if (isLinked()) {
                    return@doWork
                }
                if (compare(context.allLinker, list)) {
                    return@doWork
                }
                val toRemve = LinkedList<QMicLinker>()
                context.allLinker.forEach { old ->
                    var isContainer = false
                    if (old.user.userId == currentRoomInfo?.anchor?.userId) {
                        isContainer = true
                    } else {
                        list.forEach { new ->
                            if (new.user.userId == old.user.userId) {
                                isContainer = true
                            }
                        }
                    }
                    if (!isContainer) {
                        toRemve.add(old)
                    }
                }
                toRemve.forEach { linck ->
                    context.mQLinkMicServiceListeners.forEach {
                        it.onLinkerLeft(linck)
                    }
                }
                context.allLinker.removeAll(toRemve)

                list.forEach { linck ->
                    //新加的麦位
                    if (context.addLinker(linck)) {
                        context.mQLinkMicServiceListeners.forEach {
                            it.onLinkerJoin(linck)
                        }
                    }
                }
            }
        }
    }

    override fun addLinkMicListener(listener: QAudienceMicHandler.LinkMicHandlerListener) {
        mLinkMicHandlerListeners.add(listener)
    }

    override fun removeLinkMicListener(listener: QAudienceMicHandler.LinkMicHandlerListener) {
        mLinkMicHandlerListeners.remove(listener)
    }

    private val mAudienceExtQNClientEventListener = object : DefaultExtQNClientEventListener {
        override fun onConnectionStateChanged(
            p0: QNConnectionState,
            p1: QNConnectionDisconnectedInfo?
        ) {
            mLinkMicHandlerListeners.forEach {
                it.onConnectionStateChanged(p0.toQConnectionState())
            }
            if (p0 == QNConnectionState.DISCONNECTED) {
                if (mMeLinker != null) {
                    stopInner(
                        true, null
                    )
                }
            }
        }
    }

    override fun attachRoomClient(client: QLiveClient) {
        super.attachRoomClient(client)
        mPlayer = playerGetter
        context.mQRtcLiveRoom = QRtcLiveRoom(AppCache.appContext)
        context.mQRtcLiveRoom.addExtraQNRTCEngineEventListener(context.mExtQNClientEventListener)
        context.mQRtcLiveRoom.addExtraQNRTCEngineEventListener(mAudienceExtQNClientEventListener)
        context.onKickCall = { linker, m ->
            if (linker.user.userId == user?.userId) {
                stopInner(true, null, false, true, m)
            }
        }
    }

    override fun onDestroyed() {
        mLinkMicHandlerListeners.clear()
        super.onDestroyed()
        context.mQRtcLiveRoom.close()
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo,isResumeUIFromFloating)
        mMicListJob.start()
    }

    override fun startLink(
        extension: HashMap<String, String>?, cameraParams: QCameraParam?,
        microphoneParams: QMicrophoneParam?, callBack: QLiveCallBack<Void>?
    ) {
        mMicListJob.cancel()
        if (currentRoomInfo == null) {
            callBack?.onError(-1, "roomInfo==null")
            mMicListJob.start(true)
            return
        }
        val linker = QMicLinker()
        linker.extension = extension
        linker.isOpenCamera = cameraParams != null
        linker.isOpenMicrophone = microphoneParams != null
        linker.userRoomID = currentRoomInfo?.liveID ?: ""
        backGround {
            doWork {
                val token = mLinkDateSource.upMic(linker)
                linker.user = user
                RtmManager.rtmClient.sendChannelMsg(
                    RtmTextMsg<QMicLinker>(
                        liveroom_miclinker_join,
                        linker
                    ).toJsonString(),
                    currentRoomInfo!!.chatID, false
                )
                cameraParams?.let {
                    context.mQRtcLiveRoom.enableCamera(it)
                }
                microphoneParams?.let {
                    context.mQRtcLiveRoom.enableMicrophone(it)
                }
                context.mQRtcLiveRoom.joinRtc(token.rtc_token, JsonUtils.toJson(linker))
//                val users = ArrayList<QNMicLinker>()
//                context.mRtcLiveRoom.mClient.remoteUsers.forEach {
//                    if (it.userID != roomInfo?.anchor?.userId) {
//                        val linck = JsonUtils.parseObject(it.userData, QNMicLinker::class.java)
//                    }
//                }
                context.mQRtcLiveRoom.publishLocal()
                mLinkMicHandlerListeners.forEach {
                    it.onRoleChange(true)
                }
                context.mExtQNClientEventListener.onUserJoined(
                    user?.userId ?: "",
                    JsonUtils.toJson(linker)
                )
                mPlayer?.onLinkStatusChange(true)
                callBack?.onSuccess(null)
            }
            catchError {
                mMicListJob.start(true)
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun isLinked(): Boolean {
        return mMeLinker != null
    }

    override fun stopLink(callBack: QLiveCallBack<Void>?) {
        stopInner(false, callBack)
    }

    private fun stopInner(
        force: Boolean,
        callBack: QLiveCallBack<Void>?,
        isPositive: Boolean = true, isKick: Boolean = false, kick: UidMsgMode? = null
    ) {
        if (mMeLinker == null) {
            callBack?.onError(-1, "user is not on mic")
            return
        }
        backGround {
            doWork {
                try {
                    mLinkDateSource.downMic(mMeLinker!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val mode = UidMode().apply {
                    uid = user?.userId?:""
                }
                if (isPositive) {
                    try {
                        RtmManager.rtmClient.sendChannelMsg(
                            RtmTextMsg<UidMode>(
                                liveroom_miclinker_left,
                                mode
                            ).toJsonString(),
                            currentRoomInfo!!.chatID, false
                        )
                    } catch (e: RtmException) {
                        e.printStackTrace()
                    }
                }
                if (isKick) {
                    try {
                        RtmManager.rtmClient.sendChannelMsg(
                            RtmTextMsg<UidMsgMode>(
                                liveroom_miclinker_kick,
                                kick!!
                            ).toJsonString(),
                            currentRoomInfo!!.chatID, true
                        )
                    } catch (e: RtmException) {
                        e.printStackTrace()
                    }
                }
                context.mQRtcLiveRoom.leaveBgDestroyTrack()
                QLiveLogUtil.d("leaveBgDestroyTrack", "onLinkStatusChange")
                mPlayer?.onLinkStatusChange(false)
                QLiveLogUtil.d("leaveBgDestroyTrack", "onLinkStatusChange")
                context.mExtQNClientEventListener.onUserLeft(
                    user?.userId ?: ""
                )
                context.removeLinker(user!!.userId)
                mLinkMicHandlerListeners.forEach {
                    it.onRoleChange(false)
                }
                mMicListJob.start(true)
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    private suspend fun stopSuspend(
        force: Boolean,
        isPositive: Boolean = true, isKick: Boolean = false, kick: UidMsgMode? = null
    ) = suspendCoroutine<Unit> { ct ->
        stopInner(force, object : com.qlive.core.QLiveCallBack<java.lang.Void> {
            override fun onError(code: Int, msg: String?) {
                ct.resume(Unit)
            }

            override fun onSuccess(data: Void?) {
                ct.resume(Unit)
            }
        }, isPositive, isKick, kick)

    }

    override fun onLeft() {
        super.onLeft()
        mMicListJob.cancel()
        if (mMeLinker != null) {
            stopInner(true, null)
        }
    }

    override fun switchCamera(callBack: QLiveCallBack<QCameraFace>?) {
        if (mMeLinker == null) {
            callBack?.onError(-1, "not in seat")
            return
        }
        context.mQRtcLiveRoom.switchCamera() { it, msg ->
            if (it !== null) {
                callBack?.onSuccess(
                    if (it) {
                        QCameraFace.FRONT
                    } else {
                        QCameraFace.BACK
                    }
                )
            } else {
                callBack?.onError(-1, msg)
            }
        }
    }

    override fun muteCamera(muted: Boolean, callBack: QLiveCallBack<Boolean>?) {

        if (mMeLinker == null) {
            return
        }
        val mode = MuteMode().apply {
            uid = user?.userId?:""
            mute = muted
        }
        backGround {
            doWork {
                if (context.mQRtcLiveRoom.muteLocalCamera(muted)) {
                    mLinkDateSource.switch(
                        mMeLinker!!, false, !muted
                    )
                    RtmManager.rtmClient.sendChannelMsg(
                        RtmTextMsg<MuteMode>(
                            liveroom_miclinker_camera_mute,
                            mode
                        ).toJsonString(),
                        currentRoomInfo!!.chatID, true
                    )
                    mMeLinker?.isOpenCamera = !muted
                    callBack?.onSuccess(true)
                } else {
                    callBack?.onSuccess(false)
                }
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun muteMicrophone(muted: Boolean, callBack: QLiveCallBack<Boolean>?) {

        if (mMeLinker == null) {
            return
        }
        val mode = MuteMode().apply {
            uid = user?.userId?:""
            mute = muted
        }
        backGround {
            doWork {
                if (context.mQRtcLiveRoom.muteLocalMicrophone(muted)) {
                    mLinkDateSource.switch(
                        mMeLinker!!, true, !muted
                    )
                    RtmManager.rtmClient.sendChannelMsg(
                        RtmTextMsg<MuteMode>(
                            liveroom_miclinker_microphone_mute,
                            mode
                        ).toJsonString(),
                        currentRoomInfo!!.chatID, true
                    )
                    mMeLinker?.isOpenMicrophone = !muted
                    callBack?.onSuccess(true)
                } else {
                    callBack?.onSuccess(false)
                }
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun setVideoFrameListener(frameListener: QVideoFrameListener?) {
        context.mQRtcLiveRoom.setVideoFrameListener(QVideoFrameListenerWrap(frameListener))
    }

    override fun setAudioFrameListener(frameListener: QAudioFrameListener?) {
        context.mQRtcLiveRoom.setAudioFrameListener(QAudioFrameListenerWrap(frameListener))
    }

    override fun setDefaultBeauty(beautySetting: QBeautySetting) {
        context.mQRtcLiveRoom.localVideoTrack?.setBeauty(beautySetting.toQNBeautySetting())
    }

    private val playerGetter by lazy {
        (client as QPlayerProvider).playerGetter.invoke()
    }

    override suspend fun checkLeave() {
        if (isLinked) {
            stopSuspend(true)
        }
    }
}