package com.qlive.rtminvitation

import android.text.TextUtils
import com.qlive.rtm.RtmCallBack
import com.qlive.rtm.RtmException
import com.qlive.rtm.RtmManager.rtmClient
import com.qlive.rtm.msg.RtmTextMsg
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun InvitationProcessor.suspendInvite(
    msg: String,
    peerId: String,
    channelId: String,
    timeoutThreshold: Long
) = suspendCoroutine<Invitation> { coti ->
    val invitationMsg = createInvitation(msg, peerId, channelId, timeoutThreshold)
    val rtmTextMsg = RtmTextMsg(
        InvitationProcessor.ACTION_SEND,
        invitationMsg
    )
    val call: RtmCallBack = object : RtmCallBack {
        override fun onSuccess() {
            addTimeOutRun(invitationMsg.invitation)
            coti.resume(invitationMsg.invitation)
        }
        override fun onFailure(code: Int, msg: String) {
            coti.resumeWithException(RtmException(code, msg))
        }
    }
    if (TextUtils.isEmpty(channelId)) {
        rtmClient.sendC2cCMDMsg(rtmTextMsg.toJsonString(), peerId, false, call)
    } else {
        rtmClient.sendChannelCMDMsg(rtmTextMsg.toJsonString(), channelId, false, call)
    }
}