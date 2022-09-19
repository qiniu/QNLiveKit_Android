package com.qlive.danmakuservice;


import com.qlive.core.been.QLiveUser;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 弹幕实体
 */
public class QDanmaku implements Serializable {
    public static String action_danmu = "living_danmu";
    /**
     * 发送方用户
     */
    public QLiveUser sendUser;
    /**
     * 弹幕内容
     */
    public String content;
    /**
     * 发送方所在房间ID
     */
    public String senderRoomID;
    /**
     * 扩展字段
     */
    public HashMap<String, String> extension;
}


