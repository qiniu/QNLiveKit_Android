package com.qlive.pkservice

import android.content.Context
import com.qlive.core.*
import com.qlive.coreimpl.BaseService
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.liblog.QLiveLogUtil
import java.util.*

internal class AudiencePKSynchro() : BaseService() {

    private val mPKDateSource = PKDataSource()
    private val mLiveDataSource = QLiveDataSource()
    var mListenersCall: (() -> LinkedList<QPKServiceListener>)? = null
    var mPKSession: QPKSession? = null
        private set
    var needSynchro = false

    private val repeatSynchroJob = com.qlive.coreimpl.Scheduler(10000) {
        if (currentRoomInfo == null) {
            return@Scheduler
        }
        //没有人注册监听就不同步
        if (!needSynchro) {
            return@Scheduler
        }
        backGround {
            doWork {
                if (currentRoomInfo?.pkID?.isEmpty() == false) {
                    //当前房间在PK
                    val info = mPKDateSource.getPkInfo(currentRoomInfo?.pkID ?: "")
                    if (info.status == PKStatus.RelaySessionStatusStopped.intValue && mPKSession != null) {
                        currentRoomInfo?.pkID = ""
                        mListenersCall?.invoke()?.forEach {
                            it.onStop(mPKSession!!, -1, "time out")
                        }
                        mPKSession = null
                    }
                } else {
                    val reFreshRoom = currentRoomInfo ?: return@doWork
                    if (reFreshRoom.pkID.isNotEmpty() && mPKSession == null) {
                        val info = mPKDateSource.getPkInfo(reFreshRoom.pkID ?: "")
                        if (info.status == PKStatus.RelaySessionStatusSuccess.intValue) {
                            val recever = mLiveDataSource.searchUserByUserId(info.recvUserId)
                            val inver = mLiveDataSource.searchUserByUserId(info.initUserId)
                            val pk = fromPkInfo(info, inver, recever)
                            mPKSession = pk
                            currentRoomInfo?.pkID = reFreshRoom.pkID
                            mListenersCall?.invoke()?.forEach {
                                it.onStart(mPKSession!!)
                            }
                        }
                    }
                }
            }
            catchError {
            }
        }
    }

    private val mQPKServiceListener = object :
        QPKServiceListener {

        override fun onStart(pkSession: QPKSession) {
            mPKSession = pkSession
            // repeatSynchroJob.start()
        }

        override fun onStop(pkSession: QPKSession, code: Int, msg: String) {
            mPKSession = null
            // repeatSynchroJob.cancel()
            currentRoomInfo?.pkID = ""
        }

        override fun onStartTimeOut(pkSession: QPKSession) {
        }
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        mListenersCall?.invoke()?.add(mQPKServiceListener)
    }

    /**
     * 进入回
     * @param user
     */
    override fun onEntering(liveId: String, user: QLiveUser) {
    }

    override fun onJoined(roomInfo: QLiveRoomInfo) {
        super.onJoined(roomInfo)
        if (!roomInfo.pkID.isEmpty()) {
            backGround {
                doWork {
                    val info = mPKDateSource.getPkInfo(roomInfo.pkID ?: "")
                    QLiveLogUtil.d("getPkInfo", "mPKDateSource.getPkInfo( ${info.status}")
                    if (info.status == PKStatus.RelaySessionStatusSuccess.intValue) {
                        val recever = mLiveDataSource.searchUserByUserId(info.recvUserId)
                        val inver = mLiveDataSource.searchUserByUserId(info.initUserId)
                        val pk = fromPkInfo(info, inver, recever)
                        mPKSession = pk
                        mListenersCall?.invoke()?.forEach {
                            it.onStart(pk)
                        }
                    }
                }
                catchError {

                }
                onFinally {
                    repeatSynchroJob.start(true)
                }
            }
        } else {
            repeatSynchroJob.start(true)
        }
    }

    override fun onLeft() {
        super.onLeft()
        repeatSynchroJob.cancel()
    }

    override fun onDestroyed() {
        repeatSynchroJob.cancel()
        super.onDestroyed()
    }

    private fun fromPkInfo(info: PKInfo, inver: QLiveUser, recver: QLiveUser): QPKSession {
        return QPKSession().apply {
            //PK场次ID
            sessionID = info.sid
            //发起方
            initiator = inver
            //接受方
            receiver = recver
            //发起方所在房间
            initiatorRoomID = info.initRoomId
            //接受方所在房间
            receiverRoomID = info.recvRoomId
            //扩展字段
            extension = info.extensions
            //pk 状态 0邀请过程  1pk中 2结束 其他自定义状态比如惩罚时间
            status = info.status
            //pk开始时间戳
            startTimeStamp = info.startAt
            if (info.startAt <= 0) {
                startTimeStamp = info.createdAt
            }
        }
    }
}