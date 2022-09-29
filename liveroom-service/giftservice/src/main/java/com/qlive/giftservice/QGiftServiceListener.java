package com.qlive.giftservice;

/**
 * 礼物监听
 */
public interface QGiftServiceListener {
    /**
     * 收到礼物消息
     * @param giftMsg
     */
    void onReceivedGiftMsg(QGiftMsg giftMsg);
}
