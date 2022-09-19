package com.qlive.pubchatservice;

import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;

/**
 * 公屏服务
 */
public interface QPublicChatService extends QLiveService {


    /**
     * 发送 公聊
     *
     * @param msg      公屏消息内容
     * @param callBack 操作回调
     */
    public void sendPublicChat(String msg, QLiveCallBack<QPublicChat> callBack);

    /**
     * 发送 进入消息
     *
     * @param msg      消息内容
     * @param callBack 操作回调
     */
    public void sendWelCome(String msg, QLiveCallBack<QPublicChat> callBack);

    /**
     * 发送 拜拜
     *
     * @param msg      消息内容
     * @param callBack 操作回调
     */
    public void sendByeBye(String msg, QLiveCallBack<QPublicChat> callBack);

    /**
     * 点赞
     *
     * @param msg 消息内容
     *            * @param callBack 操作回调
     */
    public void sendLike(String msg, QLiveCallBack<QPublicChat> callBack);

    /**
     * 自定义要显示在公屏上的消息
     *
     * @param action   消息code 用来区分要做什么响应
     * @param msg      消息内容
     * @param callBack 回调
     */
    public void sendCustomPubChat(String action, String msg, QLiveCallBack<QPublicChat> callBack);


    /**
     * 往本地公屏插入消息 不发送到远端
     */
    public void pubMsgToLocal(QPublicChat chatModel);

    /**
     * 添加监听
     *
     * @param lister
     */
    public void addServiceLister(QPublicChatServiceLister lister);

    /**
     * 移除监听
     *
     * @param lister
     */
    public void removeServiceLister(QPublicChatServiceLister lister);
}
