package com.qlive.giftservice;

import com.qlive.core.been.QLiveUser;

import java.io.Serializable;

/**
 * 礼物消息
 */
public class QGiftMsg implements Serializable {

    public static String GIFT_ACTION = "gift_notify";

    /**
     * 所在直播间
     */
    public String liveID;
    /**
     * 礼物信息
     */
    public QGift gift;
    /**
     * 发送者信息
     */
    public QLiveUser sender;
}
