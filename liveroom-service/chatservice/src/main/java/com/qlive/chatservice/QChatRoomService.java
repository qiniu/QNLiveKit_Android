package com.qlive.chatservice;


import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;
import com.qlive.core.been.QLiveUser;

import java.util.List;

/**
 * 聊天室服务
 */
public interface QChatRoomService extends QLiveService {

    /**
     * 添加聊天室监听
     *
     * @param chatServiceListener 监听
     */
    public void addServiceListener(QChatRoomServiceListener chatServiceListener);

    /**
     * 移除聊天室监听
     *
     * @param chatServiceListener 监听
     */
    public void removeServiceListener(QChatRoomServiceListener chatServiceListener);

    /**
     * 发c2c消息
     *
     * @param isCMD    是不是信令消息
     * @param msg      消息内容
     * @param memberID 成员im ID
     * @param callBack 回调
     */
    void sendCustomC2CMsg(boolean isCMD, String msg, String memberID, QLiveCallBack<Void> callBack);

    /**
     * 发群消息
     *
     * @param isCMD    是不是信令消息
     * @param msg      消息内容
     * @param callBack 回调
     */
    void sendCustomGroupMsg(boolean isCMD, String msg, QLiveCallBack<Void> callBack);

    /**
     * 踢人
     *
     * @param msg      消息内容
     * @param memberID 成员im ID
     * @param callBack 回调
     */
    void kickUser(String msg, String memberID, QLiveCallBack<Void> callBack);

    /**
     * 禁言
     *
     * @param isMute   是否禁言
     * @param msg      消息内容
     * @param memberID 成员im ID
     * @param duration 禁言时常
     * @param callBack 回调
     */
    void muteUser(boolean isMute, String msg, String memberID, long duration, QLiveCallBack<Void> callBack);

    /**
     * 禁言列表
     */
    void getBannedMembers(QLiveCallBack<List<QLiveUser>> callBack);

    /**
     * 拉黑
     *
     * @param isBlock  是否拉黑
     * @param memberID 成员im ID
     * @param callBack 回调
     */
    void blockUser(boolean isBlock, String memberID, QLiveCallBack<Void> callBack);

    /**
     * 黑名单列表
     *
     * @param forceRefresh 是否强制拉服务端数据否则缓存
     * @param callBack
     */
    void getBlockList(boolean forceRefresh, QLiveCallBack<List<QLiveUser>> callBack);

    /**
     * 添加管理员
     *
     * @param memberID 成员im ID
     * @param callBack 回调
     */
    void addAdmin(String memberID, QLiveCallBack<Void> callBack);

    /**
     * 移除管理员
     *
     * @param msg
     * @param memberID 成员im ID
     * @param callBack 回调
     */
    void removeAdmin(String msg, String memberID, QLiveCallBack<Void> callBack);
}
