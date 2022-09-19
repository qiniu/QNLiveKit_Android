package com.qlive.core;

import com.qlive.core.been.QInvitation;

/**
 * 邀请监听
 */
public interface QInvitationHandlerListener {
    /**
     * 收到申请/邀请
     * @param invitation
     */
    void onReceivedApply(QInvitation invitation);   //收到申请/邀请

    /**
     * 对方取消申请
     * @param invitation
     */
    void onApplyCanceled(QInvitation invitation);   //对方取消申请

    /**
     * 申请/邀请 超时
     * @param invitation
     */
    void onApplyTimeOut(QInvitation invitation);    //申请/邀请 超时

    /**
     * 被接受
     * @param invitation
     */
    void onAccept(QInvitation invitation);          //被接受

    /**
     * 被拒绝
     * @param invitation
     */
    void onReject(QInvitation invitation);          //被拒绝
}
