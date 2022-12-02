package com.qlive.rtminvitation

import com.qlive.jsonutil.JsonUtils
import com.qlive.rtm.RtmManager
import com.qlive.rtm.RtmMsgListener
import com.qlive.rtm.msg.TextMsg
import com.qlive.rtminvitation.InvitationProcessor.ACTION_HANGUP

/**
 * 邀请系统
 */
object InvitationManager {

    private var mInvitationProcessor = ArrayList<InvitationProcessor>()

    private var mRtmMsgIntercept = object : RtmMsgListener {
        override fun onNewMsg(msg: TextMsg): Boolean {
            var isIntercept = false
            val action = msg.optAction()
            if (
                (action == InvitationProcessor.ACTION_SEND
                        || action == InvitationProcessor.ACTION_CANCEL
                        || action == InvitationProcessor.ACTION_ACCEPT
                        || action == InvitationProcessor.ACTION_REJECT)

            ) {
                isIntercept = true
                val invitationMsgModel =
                    JsonUtils.parseObject(msg.optData(), InvitationMsg::class.java)

                val invitation = invitationMsgModel?.invitation ?: return true
                val invitationName = invitationMsgModel.invitationName

//                if(invitation?.receiver == RtmManager.rtmClient.getLoginUserId()
//                    || invitation?.initiatorUid == RtmManager.rtmClient.getLoginUserId()
//
//                    ||invitation?.receiver == RtmManager.rtmClient.getLoginUserIMUId()
//                    || invitation?.initiatorUid == RtmManager.rtmClient.getLoginUserIMUId()
//                ){
                //ios没传
                if (invitation.flag <= 0) {
                    invitation.flag = (Math.random() * 100000).toInt()
                }
                mInvitationProcessor.forEach {
                    if (it.invitationName == invitationName) {
                        when (action) {
                            InvitationProcessor.ACTION_SEND -> {
                                invitation.initiatorUid = msg.fromID
                                it.addTimeOutRun(invitation)
                                it.onReceiveInvitation(invitation)
                            }
                            InvitationProcessor.ACTION_CANCEL -> {
                                invitation.initiatorUid = msg.fromID
                                it.onReceiveCanceled(invitation)
                            }
                            InvitationProcessor.ACTION_ACCEPT -> {
                                invitation.initiatorUid = msg.fromID
                                it.reMoveTimeOutRun(invitation)
                                it.onInviteeAccepted(invitation)
                            }
                            InvitationProcessor.ACTION_REJECT -> {
                                invitation.initiatorUid = msg.fromID
                                it.reMoveTimeOutRun(invitation)
                                it.onInviteeRejected(invitation)
                            }
                            ACTION_HANGUP -> {
                                invitation.initiatorUid = msg.fromID
                                it.onInviteeHangUp(invitation)
                            }
                        }
                    }
                }
            }
            //   }
            return isIntercept
        }
    }

    init {
        RtmManager.addRtmC2cListener(mRtmMsgIntercept)
        RtmManager.addRtmChannelListener(mRtmMsgIntercept)
    }

    /**
     * 添加信令处理
     */
    fun addInvitationProcessor(invitationProcessor: InvitationProcessor) {
        mInvitationProcessor.add(invitationProcessor)
    }

    fun removeInvitationProcessor(invitationProcessor: InvitationProcessor) {
        mInvitationProcessor.remove(invitationProcessor)
    }
}