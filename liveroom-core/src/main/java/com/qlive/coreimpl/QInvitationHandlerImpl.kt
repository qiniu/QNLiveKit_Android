package com.qlive.coreimpl

import com.qlive.rtm.RtmCallBack
import com.qlive.rtminvitation.*
import com.qlive.jsonutil.JsonUtils
import com.qlive.core.QClientLifeCycleListener
import com.qlive.core.*
import com.qlive.core.QLiveErrorCode.NOT_A_ROOM_MEMBER
import com.qlive.core.been.QInvitation
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import java.util.*
import kotlin.collections.HashMap

open class QInvitationHandlerImpl(private val ivName: String) : QInvitationHandler,
    QClientLifeCycleListener {
    protected var user: QLiveUser? = null
    protected var currentRoomInfo: QLiveRoomInfo? = null
    private val mListeners = LinkedList<QInvitationHandlerListener>()
    private val invitationMap = HashMap<Int, Invitation>()
    private val dataSource = QLiveDataSource()
    private val mInvitationProcessor =
        InvitationProcessor(ivName,
            object : InvitationCallBack {
                override fun onReceiveInvitation(invitation: Invitation) {
                    val qInvitation =
                        JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
                    if (qInvitation.receiverRoomID != currentRoomInfo?.liveID) {
                        return
                    }
                    qInvitation.invitationID = invitation.flag
                    invitationMap[invitation.flag] = invitation
                    mListeners.forEach { it.onReceivedApply(qInvitation) }
                }

                override fun onInvitationTimeout(invitation: Invitation) {
                    val qInvitation =
                        JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
                    invitationMap.remove(invitation.flag)
                    if (qInvitation.initiatorRoomID != currentRoomInfo?.liveID) {
                        return
                    }
                    qInvitation.invitationID = invitation.flag
                    mListeners.forEach { it.onApplyTimeOut(qInvitation) }

                }

                override fun onReceiveCanceled(invitation: Invitation) {
                    val qInvitation =
                        JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
                    qInvitation.invitationID = invitation.flag
                    invitationMap.remove(invitation.flag)
                    if (qInvitation.receiverRoomID != currentRoomInfo?.liveID) {
                        return
                    }
                    mListeners.forEach { it.onApplyCanceled(qInvitation) }

                }

                override fun onInviteeAccepted(invitation: Invitation) {
                    val qInvitation =
                        JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
                    qInvitation.invitationID = invitation.flag
                    invitationMap.remove(invitation.flag)
                    if (qInvitation.initiatorRoomID != currentRoomInfo?.liveID) {
                        return
                    }
                    mListeners.forEach { it.onAccept(qInvitation) }
                }

                override fun onInviteeRejected(invitation: Invitation) {
                    val qInvitation =
                        JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
                    qInvitation.invitationID = invitation.flag
                    invitationMap.remove(invitation.flag)
                    if (qInvitation.initiatorRoomID != currentRoomInfo?.liveID) {
                        return
                    }
                    mListeners.forEach { it.onReject(qInvitation) }
                }
            })


    override fun apply(
        expiration: Long,
        receiverRoomID: String,
        receiverUID: String,
        extension: HashMap<String, String>?,
        callBack: QLiveCallBack<QInvitation>?
    ) {
        if (currentRoomInfo == null) {
            callBack?.onError(NOT_A_ROOM_MEMBER, "roomInfo==null")
            return
        }
        backGround {
            doWork {
                val receiver = dataSource.searchUserByUserId(receiverUID)
                val pkInvitation = QInvitation()
                pkInvitation.extension = extension
                pkInvitation.initiator = user
                pkInvitation.initiatorRoomID = currentRoomInfo?.liveID
                pkInvitation.receiver = receiver
                pkInvitation.receiverRoomID = receiverRoomID
                val iv = mInvitationProcessor.suspendInvite(
                    JsonUtils.toJson(pkInvitation),
                    receiver.imUid, "", expiration
                )
                pkInvitation.invitationID = iv.flag
                invitationMap[iv.flag] = iv
                callBack?.onSuccess(pkInvitation)
            }

            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun cancelApply(invitationID: Int, callBack: QLiveCallBack<Void?>?) {
        mInvitationProcessor.cancel(invitationMap[invitationID], object : RtmCallBack {
            override fun onSuccess() {
                invitationMap.remove(invitationID)
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })
    }

    override fun accept(
        invitationID: Int,
        extension: HashMap<String, String>?,
        callBack: QLiveCallBack<Void>?
    ) {
        val invitation = invitationMap[invitationID]
        if (invitation == null) {
            callBack?.onError(-1, "invitation==null")
            return
        }
        val linkInvitation =
            JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
        extension?.entries?.forEach {
            linkInvitation.extension[it.key] = it.value
        }
        invitation.msg = JsonUtils.toJson(linkInvitation)
        mInvitationProcessor.accept(invitation, object : RtmCallBack {
            override fun onSuccess() {
                invitationMap.remove(invitationID)
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })
    }

    override fun reject(
        invitationID: Int,
        extension: HashMap<String, String>?,
        callBack: QLiveCallBack<Void>?
    ) {
        val invitation = invitationMap[invitationID]
        if (invitation == null) {
            callBack?.onError(-1, "invitation==null")
            return
        }
        val linkInvitation =
            JsonUtils.parseObject(invitation.msg, QInvitation::class.java) ?: return
        extension?.entries?.forEach {
            linkInvitation.extension[it.key] = it.value
        }
        invitation.msg = JsonUtils.toJson(linkInvitation)
        mInvitationProcessor.reject(invitation, object : RtmCallBack {
            override fun onSuccess() {
                invitationMap.remove(invitationID)
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })
    }

    override fun removeInvitationHandlerListener(listener: QInvitationHandlerListener) {
        mListeners.remove(listener)
    }

    override fun addInvitationHandlerListener(listener: QInvitationHandlerListener) {
        mListeners.add(listener)
    }


    /**
     * 进入回调
     *
     * @param user
     */
    override fun onEntering(liveId: String, user: QLiveUser) {
        this.user = user
    }

    /**
     * 加入回调
     *
     * @param roomInfo
     */
    override fun onJoined(roomInfo: QLiveRoomInfo) {
        this.currentRoomInfo = roomInfo
    }

    /**
     * 离开回调
     */
    override fun onLeft() {
        user = null
    }

    fun attach() {
        InvitationManager.addInvitationProcessor(mInvitationProcessor)
    }

    /**
     * 销毁回调
     */
    override fun onDestroyed() {
        mListeners.clear()
        InvitationManager.removeInvitationProcessor(mInvitationProcessor)
    }


}