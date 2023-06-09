package com.qlive.pkservice

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg
import com.qlive.rtclive.DefaultExtQNClientEventListener
import com.qlive.rtclive.QRTCProvider
import com.qlive.rtclive.QRtcLiveRoom
import com.qiniu.droid.rtc.*
import com.qlive.avparam.QMixStreaming
import com.qlive.jsonutil.JsonUtils
import com.qlive.core.*
import com.qlive.liblog.QLiveLogUtil
import com.qlive.coreimpl.BaseService
import com.qlive.avparam.RtcException
import com.qlive.coreimpl.*
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.avparam.QPushRenderView
import com.qlive.core.QLiveErrorCode.PK_STATUS_ERROR
import com.qlive.core.been.QExtension
import com.qlive.rtclive.RTCRenderView
import com.qlive.rtm.msg.TextMsg
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class QPKServiceImpl : QPKService, BaseService() {

    companion object {
        const val LIVE_ROOM_PK_START = "liveroom_pk_start"
        const val LIVE_ROOM_PK_STOP = "liveroom_pk_stop"
        const val PK_EXTENDS_NOTIFY = "pk_extends_notify"
        val PK_STATUS_OK = PKStatus.RelaySessionStatusSuccess.intValue
    }

    private val mPKDateSource = PKDataSource()
    private val pkPKInvitationHandlerImpl = QInvitationHandlerImpl("liveroom_pk_invitation")
    private val mServiceListeners = LinkedList<QPKServiceListener>()
    private var mQPKMixStreamAdapter: QPKMixStreamAdapter? = null
    private val mAudiencePKSynchro = AudiencePKSynchro().apply {
        mListenersCall = {
            mServiceListeners
        }
    }
    var mPKSession: QPKSession? = null
        private set

    private var mPKSessionTemp: QPKSession? = null

    private var trackUidTemp = ""
    private fun checkReceivePk(pkTemp: QPKSession?, uidTem: String) {
        QLiveLogUtil.d(
            "startMediaRelay",
            "checkReceivePk pk checkReceivePk ${(pkTemp) == null} $uidTem "
        )
        if (pkTemp != null) {
            mPKSessionTemp = pkTemp
        }
        if (!TextUtils.isEmpty(uidTem)) {
            trackUidTemp = uidTem
        }
        if (mPKSessionTemp != null && !TextUtils.isEmpty(trackUidTemp) &&
            mPKSessionTemp?.initiator?.userId == trackUidTemp
        ) {
            //开始收到pk
            backGround {
                doWork {

                    val pkOutline = mPKDateSource.recevPk(mPKSessionTemp?.sessionID ?: "")
                    val room: QRtcLiveRoom = rtcRoomGetter
                    //转发
                    val sourceInfo = QNMediaRelayInfo(room.roomName, room.roomToken)
                    val configuration = QNMediaRelayConfiguration(sourceInfo)

                    val tokens: Array<String> =
                        pkOutline.relay_token.split(":".toRegex()).toTypedArray()
                    val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
                    val json: JSONObject = JSONObject(b64)
                    val mAppId = json.optString("appId")
                    val peerRoomName = json.optString("roomName")

                    mPKDateSource.ackACKPk(mPKSessionTemp?.sessionID ?: "")
                    val destInfo1 = QNMediaRelayInfo(peerRoomName, pkOutline.relay_token)
                    configuration.addDestRoomInfo(destInfo1)
                    QLiveLogUtil.d("startMediaRelay", "checkReceivePk ")
                    startMediaRelay(peerRoomName, room.mClient, configuration)

                    //pk 接收方收到 邀请方
                    mPKSession = mPKSessionTemp
                    mPKSession?.status = PK_STATUS_OK

                    try {
                        val info = mPKDateSource.getPkInfo(mPKSession!!.sessionID)
                        mPKSession!!.startTimeStamp = info.startAt
                        if (info.startAt <= 0) {
                            mPKSession!!.startTimeStamp = info.createdAt
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    try {
                        //群信号
                        RtmManager.rtmClient.sendChannelCMDMsg(
                            RtmTextMsg<QPKSession>(
                                LIVE_ROOM_PK_START,
                                mPKSession
                            ).toJsonString(), currentRoomInfo!!.chatID, false
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    mServiceListeners.forEach {
                        it.onStart(mPKSession!!)
                    }
                    //混流
                    resetMixStream(mPKSession?.initiator?.userId ?: "")
                    QLiveLogUtil.d("pk 接收方确认回复pk成功 ")
                }
                catchError {
                    QLiveLogUtil.d("pk 接收方确认回复pk 错误 ${it.getCode()} ${it.message}")
                    mPKSessionTemp?.let {
                        mServiceListeners.forEach {
                            it.onStartTimeOut(mPKSessionTemp!!)
                        }
                    }
                }
                onFinally {
                    mPKSessionTemp = null
                    trackUidTemp = ""
                }
            }
        } else {
            QLiveLogUtil.d("pk check not  param march ")
        }
    }

    private val mC2cListener = object : RtmMsgListener {
        override fun onNewMsg(msg: TextMsg): Boolean {
            if (msg.optAction() == LIVE_ROOM_PK_START) {
                QLiveLogUtil.d("pk 接收方收到pk holle ")
                val pk =
                    JsonUtils.parseObject(msg.optData(), QPKSession::class.java) ?: return true
                checkReceivePk(pk, "")
            }

            if (msg.optAction() == LIVE_ROOM_PK_STOP) {
                val pk =
                    JsonUtils.parseObject(msg.optData(), QPKSession::class.java) ?: return true
                if (mPKSession?.sessionID == pk.sessionID) {
                    loopStop()
                }
            }

            return false
        }
    }

    private val groupListener = object : RtmMsgListener {
        override fun onNewMsg(msg: TextMsg): Boolean {
            if (msg.toID != currentRoomInfo?.chatID) {
                return false
            }
            when (msg.optAction()) {
                LIVE_ROOM_PK_START -> {
                    val pk = JsonUtils.parseObject(msg.optData(), QPKSession::class.java)
                        ?: return true
                    mServiceListeners.forEach {
                        it.onStart(pk)
                    }
                    return true
                }

                LIVE_ROOM_PK_STOP -> {
                    val pk = JsonUtils.parseObject(msg.optData(), QPKSession::class.java)
                        ?: return true
                    mServiceListeners.forEach {
                        it.onStop(pk, 1, "")
                    }
                    return true
                }

                PK_EXTENDS_NOTIFY -> {
                    val pkExt = JsonUtils.parseObject(msg.optData(), PKExtendsNotify::class.java)
                        ?: return true
                    val pkS = currentPKingSession()
                    if (pkExt.sid != pkS?.sessionID) {
                        QLiveLogUtil.d("PK_EXTENDS_NOTIFY but ${pkExt.sid} != ${pkS?.sessionID}")
                        return true
                    }

                    pkExt.extendsX.forEach { ext ->
                        pkS?.extension?.put(ext.key, ext.value)
                        mServiceListeners.forEach {
                            it.onPKExtensionChange(QExtension().apply {
                                key = ext.key
                                value = ext.value
                            })
                        }
                    }
                    return true
                }
            }
            return false
        }
    }

    private var timeoutJob: Job? = null
    private fun startTimeOutJob(timeoutTimestamp: Long) {
        timeoutJob = GlobalScope.launch(Dispatchers.Main) {
            try {

                delay(timeoutTimestamp)
                QLiveLogUtil.d("pk 邀请方等待超时 ")
                if (mPKSession == null) {
                    return@launch
                }
                mServiceListeners.forEach {
                    it.onStartTimeOut(mPKSession!!)
                }
                try {
                    mPKDateSource.stopPk(mPKSession?.sessionID ?: "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mPKSession = null
                stopMediaRelay()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val defaultExtQNClientEventListener = object : DefaultExtQNClientEventListener {
        override fun onUserJoined(p0: String, p1: String?) {
            super.onUserJoined(p0, p1)
            if (p0 == mPKSession?.receiver?.userId) {
                QLiveLogUtil.d("pk 邀请方收到对方流 ")
                // 邀请放 收到 接收放确认了
                backGround {
                    doWork {
                        timeoutJob?.cancel()
                        mPKSession?.status = PK_STATUS_OK

                        try {
                            val info = mPKDateSource.getPkInfo(mPKSession!!.sessionID)
                            mPKSession!!.startTimeStamp = info.startAt
                            if (info.startAt <= 0) {
                                mPKSession!!.startTimeStamp = info.createdAt
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            //群信号
                            RtmManager.rtmClient.sendChannelCMDMsg(
                                RtmTextMsg<QPKSession>(
                                    LIVE_ROOM_PK_START,
                                    mPKSession
                                ).toJsonString(), currentRoomInfo!!.chatID, false
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        mServiceListeners.forEach {
                            it.onStart(mPKSession!!)
                        }
                        //混流
                        resetMixStream(mPKSession?.receiver?.userId ?: "")

                    }
                    catchError {
                        QLiveLogUtil.d(it.message ?: "")
                        it.printStackTrace()
                    }
                }
            } else {
                if (TextUtils.isEmpty(p1)) {
                    QLiveLogUtil.d("pk 接收方收到对方流 ")
                    checkReceivePk(null, p0)
                }
            }
        }

        override fun onUserLeft(p0: String) {
            super.onUserLeft(p0)
            if (mPKSession == null) {
                return
            }
            if (p0 == mPKSession?.receiver?.userId || p0 == mPKSession?.initiator?.userId) {
                QLiveLogUtil.d("pk 对方离开房间 ")
                loopStop()
            }
        }
    }

    private var isLoopStopping = false
    private fun loopStop() {
        if (isLoopStopping) {
            return
        }
        if (mPKSession == null) {
            return
        }
        isLoopStopping = true
        backGround {
            doWork {
                try {
                    mPKDateSource.stopPk(mPKSession?.sessionID ?: "")
                } catch (e: Exception) {
                    e.printStackTrace()
                    QLiveLogUtil.d("pk 对方离开房间 上报结束失败 ${e.message} ")
                }
                stopMediaRelay()
                try {
                    RtmManager.rtmClient.sendChannelCMDMsg(
                        RtmTextMsg<QPKSession>(
                            LIVE_ROOM_PK_STOP,
                            mPKSession
                        ).toJsonString(), currentRoomInfo!!.chatID, false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    QLiveLogUtil.d("pk 结束信令发送失败 ")
                }
                if (mPKSession == null) {
                    return@doWork
                }
                mServiceListeners.forEach {
                    it.onStop(mPKSession!!, 1, "peer stop")
                }
                val peer = if (mPKSession!!.initiator.userId == user?.userId) {
                    mPKSession!!.receiver.userId
                } else {
                    mPKSession!!.initiator.userId
                }
                mPKSession = null

                resetMixStream(peer)
            }
            onFinally {
                isLoopStopping = false
            }
        }
    }

    private fun resetMixStream(peerId: String) {
        if (mQPKMixStreamAdapter == null) {
            return
        }
        val mQRtcLiveRoom: QRtcLiveRoom = rtcRoomGetter
        if (mPKSession != null) {
            mQRtcLiveRoom.mMixStreamManager
                .roomUser++
            mQRtcLiveRoom.mMixStreamManager.startPkMixStreamJob(
                mQPKMixStreamAdapter!!.onPKMixStreamStart(
                    mPKSession!!
                )
            )
            val ops = mQPKMixStreamAdapter?.onPKLinkerJoin(mPKSession!!)
            ops?.forEach {
                mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.put(it.uid, it)
                mQRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            mQRtcLiveRoom.mMixStreamManager.commitOpt()
        } else {
            mQRtcLiveRoom.mMixStreamManager
                .roomUser--
            if (mQRtcLiveRoom.mMixStreamManager
                    .roomUser == 1
            ) {
                mQRtcLiveRoom.mMixStreamManager.startForwardJob()
                return
            }

            val ops = ArrayList<QMixStreaming.MergeOption>();
            try {
                mQPKMixStreamAdapter?.let {
                    ops.addAll(it.onPKLinkerLeft())
                }
            } catch (e: AbstractMethodError) {
                e.printStackTrace()
            }
            if (ops.isEmpty()) {
                mQRtcLiveRoom.mMixStreamManager.startForwardJob()
                return
            }
            var mix: QMixStreaming.MixStreamParams? = null
            try {
                mix = mQPKMixStreamAdapter?.onPKMixStreamStop()
            } catch (e: AbstractMethodError) {
                e.printStackTrace()
            }
            mQRtcLiveRoom.mMixStreamManager.startMixStreamJob(mix)
            ops.forEach {
                mQRtcLiveRoom.mMixStreamManager.lastUserMergeOp.put(it.uid, it)
                mQRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                mQRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            mQRtcLiveRoom.mMixStreamManager.commitOpt()
        }
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        RtmManager.addRtmC2cListener(mC2cListener)
        RtmManager.addRtmChannelListener(groupListener)

        if (client.clientType == QClientType.PUSHER) {
            val room: QRtcLiveRoom = rtcRoomGetter
            room.addExtraQNRTCEngineEventListener(defaultExtQNClientEventListener)
            pkPKInvitationHandlerImpl.attach()
        } else {
            mAudiencePKSynchro.attachRoomClient(client, appContext)
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mServiceListeners.clear()
        RtmManager.removeRtmC2cListener(mC2cListener)
        RtmManager.removeRtmChannelListener(groupListener)

        if (client?.clientType == QClientType.PUSHER) {
            pkPKInvitationHandlerImpl.onDestroyed()
        } else {
            mAudiencePKSynchro.onDestroyed()
        }
    }

    override fun onEntering(liveId: String, user: QLiveUser) {
        super.onEntering(liveId, user)
        if (client?.clientType == QClientType.PUSHER) {
            pkPKInvitationHandlerImpl.onEntering(liveId, user)
        } else {
            mAudiencePKSynchro.onEntering(liveId, user)
        }
    }

    override fun onLeft() {
        super.onLeft()
        if (client?.clientType == QClientType.PUSHER) {
            pkPKInvitationHandlerImpl.onLeft()
        } else {
            mAudiencePKSynchro.onLeft()
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo) {
        super.onJoined(roomInfo)

        if (client?.clientType == QClientType.PUSHER) {
            pkPKInvitationHandlerImpl.onJoined(roomInfo)
        } else {
            mAudiencePKSynchro.onJoined(roomInfo)
        }
    }

    /**
     * 设置混流适配器
     * @param adapter
     */
    override fun setPKMixStreamAdapter(adapter: QPKMixStreamAdapter?) {
        mQPKMixStreamAdapter = adapter
    }

    override fun addServiceListener(serviceListener: QPKServiceListener) {
        mServiceListeners.add(serviceListener)
        mAudiencePKSynchro.needSynchro = true
    }

    override fun removeServiceListener(serviceListener: QPKServiceListener) {
        mServiceListeners.remove(serviceListener)
    }

    override fun start(
        timeoutTimestamp: Long,
        receiverRoomID: String,
        receiverUID: String,
        extensions: HashMap<String, String>?,
        callBack: QLiveCallBack<QPKSession>?
    ) {
        if (client?.clientType == QClientType.PLAYER) {
            callBack?.onError(
                QLiveErrorCode.NO_PERMISSION,
                " client?.clientType == QClientType.PLAYER"
            )
        }
        if (currentRoomInfo == null) {
            callBack?.onError(QLiveErrorCode.NOT_A_ROOM_MEMBER, " roomInfo==null")
            return
        }
        backGround {
            doWork {
                val pkOutline =
                    mPKDateSource.startPk(
                        currentRoomInfo?.liveID ?: "",
                        receiverRoomID,
                        receiverUID, extensions
                    )
                val receiver =
                    QLiveDataSource().searchUserByUserId(receiverUID)
                val pkSession = QPKSession()
                pkSession.extension = extensions
                pkSession.initiator = user
                pkSession.initiatorRoomID = currentRoomInfo?.liveID
                pkSession.receiver = receiver
                pkSession.receiverRoomID = receiverRoomID
                pkSession.sessionID = pkOutline.relay_id
                pkSession.status = pkOutline.relay_status
                pkSession.startTimeStamp = System.currentTimeMillis()
                mPKSession = pkSession

                //发c2c消息
                RtmManager.rtmClient.sendC2cCMDMsg(
                    RtmTextMsg<QPKSession>(
                        LIVE_ROOM_PK_START,
                        mPKSession
                    ).toJsonString(), receiver.imUid, false
                )
                val room: QRtcLiveRoom = rtcRoomGetter
                //转发
                val sourceInfo = QNMediaRelayInfo(room.roomName, room.roomToken)
                val configuration = QNMediaRelayConfiguration(sourceInfo)

                val tokens: Array<String> =
                    pkOutline.relay_token.split(":".toRegex()).toTypedArray()
                val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
                val json: JSONObject = JSONObject(b64)
                val mAppId = json.optString("appId")
                val peerRoomName = json.optString("roomName")

                val destInfo1 = QNMediaRelayInfo(peerRoomName, pkOutline.relay_token)
                configuration.addDestRoomInfo(destInfo1)
                QLiveLogUtil.d("startMediaRelay", "start positive ")
                startMediaRelay(peerRoomName, room.mClient, configuration)
                mPKDateSource.ackACKPk(mPKSession?.sessionID ?: "")
                startTimeOutJob(timeoutTimestamp)
                callBack?.onSuccess(pkSession)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    private suspend fun startMediaRelay(
        peerRoomName: String,
        client: QNRTCClient,
        configuration: QNMediaRelayConfiguration
    ) = suspendCoroutine<Unit> { continuation ->
        QLiveLogUtil.d("startMediaRelay", "startMediaRelay 开始转发 $peerRoomName ")
        client.startMediaRelay(configuration, object : QNMediaRelayResultCallback {
            override fun onResult(p0: MutableMap<String, QNMediaRelayState>) {
                QLiveLogUtil.d("startMediaRelay", "onResult 开始转发 ${p0[peerRoomName]?.name} ")
                if (p0[peerRoomName] == QNMediaRelayState.SUCCESS) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        RtcException(
                            -1,
                            "pk startMediaRelay" + p0[peerRoomName]?.name
                        )
                    )
                }
            }

            override fun onError(p0: Int, p1: String) {
                QLiveLogUtil.d("startMediaRelay", "onError 开始转发 $p0 $p1")
                continuation.resumeWithException(RtcException(p0, p1))
            }
        })
    }

    private suspend fun stopMediaRelay() =
        suspendCoroutine<Unit> { continuation ->
            val room: QRtcLiveRoom = rtcRoomGetter
            room.mClient.stopMediaRelay(object : QNMediaRelayResultCallback {
                override fun onResult(p0: MutableMap<String, QNMediaRelayState>) {
                    continuation.resume(Unit)
                }

                override fun onError(p0: Int, p1: String) {
                    continuation.resumeWithException(RtcException(p0, p1))
                }
            }
            )
        }

    override fun stop(callBack: QLiveCallBack<Void>?) {
        if (client?.clientType == QClientType.PLAYER) {
            callBack?.onError(
                QLiveErrorCode.NO_PERMISSION,
                " client?.clientType == QClientType.PLAYER"
            )
        }
        if (currentRoomInfo == null || mPKSession?.status != PK_STATUS_OK) {
            callBack?.onError(QLiveErrorCode.NOT_A_ROOM_MEMBER, " roomInfo==null")
            return
        }
        backGround {
            doWork {
                val peer = if (mPKSession!!.initiator.userId == user?.userId) {
                    mPKSession!!.receiver
                } else {
                    mPKSession!!.initiator
                }
                mPKDateSource.stopPk(mPKSession?.sessionID ?: "")
                stopMediaRelay()
                try {
                    RtmManager.rtmClient.sendC2cCMDMsg(
                        RtmTextMsg<QPKSession>(
                            LIVE_ROOM_PK_STOP,
                            mPKSession
                        ).toJsonString(), peer.imUid, false
                    )
                } catch (
                    e: Exception
                ) {
                    e.printStackTrace()
                }
                try {
                    RtmManager.rtmClient.sendChannelCMDMsg(
                        RtmTextMsg<QPKSession>(
                            LIVE_ROOM_PK_STOP,
                            mPKSession
                        ).toJsonString(), currentRoomInfo!!.chatID, false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    QLiveLogUtil.d("pk 结束信令发送失败 ")
                }
                mServiceListeners.forEach {
                    it.onStop(mPKSession!!, 0, "positive stop")
                }

                mPKSession = null
                callBack?.onSuccess(null)
                resetMixStream(peer.userId)

            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun updateExtension(extension: QExtension, callBack: QLiveCallBack<Void>?) {
        val currentPKingSession = currentPKingSession()
        if (currentPKingSession == null) {
            callBack?.onError(PK_STATUS_ERROR, "mPKSession==null")
            return
        }
        backGround {
            doWork {
                mPKDateSource.updatePKExt(mPKSession!!.sessionID, extension)
                currentPKingSession.extension?.put(extension.key, extension.value)
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    private suspend fun stopSuspend() = suspendCoroutine<Unit> { ct ->
        stop(object : QLiveCallBack<Void> {
            override fun onError(code: Int, msg: String?) {
                ct.resume(Unit)
            }

            override fun onSuccess(data: Void?) {
                ct.resume(Unit)
            }
        })
    }

    override suspend fun checkLeave() {
        if (client?.clientType == QClientType.PUSHER) {
            if (currentRoomInfo != null && mPKSession != null && mPKSession?.status == PK_STATUS_OK) {
                stopSuspend()
            }
        }
    }

    /**
     * 设置某人的连麦预览
     *
     * @param uid  麦上用户ID
     * @param view
     */
    override fun setPeerAnchorPreView(view: QPushRenderView) {
        val room: QRtcLiveRoom = rtcRoomGetter
        val peer = if (mPKSession!!.initiator.userId == user?.userId) {
            mPKSession!!.receiver.userId
        } else {
            mPKSession!!.initiator.userId
        }
        room.setUserCameraWindowView(peer, (view as RTCRenderView).getQNRender())
    }

    /**
     * 获得pk邀请处理
     * @return
     */
    override fun getInvitationHandler(): QInvitationHandler {
        return pkPKInvitationHandlerImpl
    }

    /**
     * 当前正在pk信息 没有PK则空
     */
    override fun currentPKingSession(): QPKSession? {
        if (client?.clientType == QClientType.PLAYER) {
            return mAudiencePKSynchro.mPKSession
        } else {
            return mPKSession
        }
    }

    /**
     * 获得rtc对象
     */
    private val rtcRoomGetter by lazy {
        (client as QRTCProvider).rtcRoomGetter.invoke()
    }

}