package com.qlive.pubchatservice;

//消息监听
public  interface QPublicChatServiceLister {

    /**
     * 收到公聊消息
     * pubChat.action 可以区分是啥类型的公聊消息
     * @param pubChat 消息实体
     */
    void onReceivePublicChat(QPublicChat pubChat);
}