package com.qlive.core;

import com.qlive.core.been.QInvitation;

import java.util.HashMap;
/**
 * 邀请处理器
 *
 */
public interface QInvitationHandler {
    /**
     * 发起邀请/申请
     *
     * @param expiration 过期时间 单位毫秒 过期后不再响应
     * @param receiverRoomID 接收方所在房间ID
     * @param receiverUID   接收方用户ID
     * @param extension     扩展字段
     * @param callBack   回调函数
     */
    void apply(
            long expiration,
            String  receiverRoomID,
            String receiverUID,
            HashMap<String, String> extension,
            QLiveCallBack<QInvitation>  callBack
    );

    /**
     * 取消邀请/申请
     *
     * @param invitationID 邀请ID
     * @param callBack
     */
    void cancelApply(int invitationID,QLiveCallBack<Void> callBack);

    /**
     * 接受对方的邀请/申请
     *
     * @param invitationID 邀请ID
     * @param extension    扩展字段
     * @param callBack
     */
    void accept(
            int invitationID,
            HashMap<String, String> extension,
            QLiveCallBack<Void> callBack
    );

    /**
     * 拒绝对方
     *
     * @param invitationID 邀请ID
     * @param extension 扩展字段
     * @param callBack
     */
    void reject(
            int invitationID,
            HashMap<String, String> extension,
            QLiveCallBack<Void> callBack
    );

    /**
     *移除监听
     *
     * @param listener
     */
    void removeInvitationHandlerListener(QInvitationHandlerListener listener); //添加邀请监听

    /**
     *添加监听
     *
     * @param listener
     */
    void addInvitationHandlerListener(QInvitationHandlerListener listener);
}
