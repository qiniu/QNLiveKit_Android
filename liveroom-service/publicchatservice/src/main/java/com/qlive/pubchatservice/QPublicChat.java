package com.qlive.pubchatservice;



import com.qlive.core.been.QLiveUser;

import java.io.Serializable;

/**
 * 公屏类型消息
 */
public class QPublicChat implements Serializable {

    /**
     * 类型 -- 加入房间欢迎
     */
    public static String action_welcome = "liveroom-welcome";
    /**
     * 类型 -- 离开房间
     */
    public static String action_bye = "liveroom-bye-bye";
    /**
     * 类型 -- 点赞
     */
    public static String action_like = "liveroom-like";
    /**
     * 类型 -- 公屏输入
     */
    public static String action_puchat = "liveroom-pubchat";

    public static String action_pubchat_custom = "liveroom-pubchat-custom";

    /**
     * 消息类型
     */
    public String action;
    /**
     * 发送方
     */
    public QLiveUser sendUser;
    /**
     * 消息体
     */
    public String content;
    /**
     * 发送方所在房间ID
     */
    public String senderRoomId;

}
