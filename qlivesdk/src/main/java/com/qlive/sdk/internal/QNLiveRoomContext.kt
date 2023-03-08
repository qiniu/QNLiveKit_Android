package com.qlive.sdk.internal

import com.qlive.core.*
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.*

import com.qlive.coreimpl.http.NetBzException
import com.qlive.coreimpl.model.HearBeatResp
import com.qlive.liblog.QLiveLogUtil
import com.qlive.sdk.QLive

internal class QNLiveRoomContext(private val mClient: QLiveClient) {

    private val serviceMap = HashMap<String, Any>()
    private val mLifeCycleListener = ArrayList<QClientLifeCycleListener>()
    var roomInfo: QLiveRoomInfo? = null
        private set
    private var liveId = ""

    private fun registerService(serviceClass: String) {
        try {
            val classStr = serviceClass + "Impl"
            val classImpl = Class.forName(classStr)
            val constructor = classImpl.getConstructor()
            val obj = constructor.newInstance() as BaseService
            serviceMap[serviceClass] = obj
            mLifeCycleListener.add(obj)
            obj.attachRoomClient(mClient, AppCache.appContext)
            if (liveId.isNotEmpty()) {
                obj.onEntering(liveId, QLive.getLoginUser())
            }
            if (roomInfo != null) {
                obj.onJoined(roomInfo!!)
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
    }

    fun <T : QLiveService> getService(serviceClass: Class<T>): T? {
        val serviceObj = serviceMap[serviceClass.name] as T?
        if (serviceObj == null) {
            registerService(serviceClass.name)
            return serviceMap[serviceClass.name] as T?
        }
        return serviceObj
    }

    private val roomDataSource = QLiveDataSource()
    private var roomStatus = QLiveStatus.OFF
    private var anchorStatus = 1
    var roomStatusChange: (status: QLiveStatus, msg: String) -> Unit = { _, _ -> }
    var heartbeatErrorCall: suspend () -> Unit = {}
    private val mHeartBeatJob = Scheduler(8000) {
        check()
    }

    fun checkStatus() {
        check()
    }

    fun forceSetStatus(qLiveStatus: QLiveStatus, msg: String) {
        if (roomStatus != qLiveStatus) {
            roomStatus = qLiveStatus
            roomStatusChange.invoke(roomStatus, msg)
        }
    }

    private fun check() {
        if (roomInfo == null) {
            return
        }
        backGround {
            doWork {
                var hearRet: HearBeatResp? = null
                try {
                    hearRet = roomDataSource.heartbeat(roomInfo?.liveID ?: "")
                } catch (e: NetBzException) {
                    if (e.code == 500) {
                        QLiveLogUtil.d("res.liveStatus 心跳超时 ")
                        //心跳超时从新进入房间
//                        if (mClient.clientType == QClientType.PUSHER) {
//                        } else {
//                            roomDataSource.joinRoom(roomInfo!!.liveID)
//                        }
                        heartbeatErrorCall.invoke()
                    }
                }
                val room = roomDataSource.refreshRoomInfo(roomInfo?.liveID ?: "")
                roomInfo?.copyRoomInfo(room)
                if (anchorStatus != room.anchorStatus) {
                    anchorStatus = room.anchorStatus
                    roomStatusChange.invoke(anchorStatus.anchorStatusToLiveStatus(), "")
                }
                hearRet ?: HearBeatResp().apply {
                    liveId = roomInfo?.liveID ?: ""
                    liveStatus = 3
                }
                if (hearRet!!.liveStatus.roomStatusToLiveStatus() != roomStatus) {
                    roomStatus = hearRet.liveStatus.roomStatusToLiveStatus()
                    roomStatusChange.invoke(roomStatus, "")
                }
                QLiveLogUtil.d("res.liveStatus ${hearRet.liveStatus}   room.anchorStatus ${room.anchorStatus} ")
            }
            catchError {
            }
        }
    }

    fun enter(liveId: String, user: QLiveUser) {
        this.liveId = liveId
        mLifeCycleListener.forEach {
            it.onEntering(liveId, user)
        }
    }

    fun leaveRoom() {
        mHeartBeatJob.cancel()
        mLifeCycleListener.forEach {
            it.onLeft()
        }
        this.roomInfo = null
    }

    suspend fun beforeLeaveRoom() {
        serviceMap.forEach {
            if (it.value is BaseService) {
                (it.value as BaseService).checkLeave()
            }
        }
    }

    fun notifyLinkRoleSwitched(isLink: Boolean){
        serviceMap.forEach {
            if (it.value is BaseService) {
                (it.value as BaseService).onLinkRoleSwitched(isLink)
            }
        }
    }

    fun joinedRoom(roomInfo: QLiveRoomInfo) {
        this.roomInfo = roomInfo
        roomStatus = roomInfo.liveStatus.roomStatusToLiveStatus()
        anchorStatus = roomInfo.anchorStatus
        mHeartBeatJob.start()
        mLifeCycleListener.forEach {
            it.onJoined(roomInfo)
        }
    }

    fun destroy() {
        mHeartBeatJob.cancel()
        mLifeCycleListener.forEach {
            it.onDestroyed()
        }
        mLifeCycleListener.clear()
        serviceMap.clear()
    }
}