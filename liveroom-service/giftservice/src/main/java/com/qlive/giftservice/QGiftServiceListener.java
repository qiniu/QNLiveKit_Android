package com.qlive.giftservice;

import org.jetbrains.annotations.NotNull;

/**
 * 礼物监听
 */
public interface QGiftServiceListener {
    /**
     * 收到礼物消息
     * @param giftMsg
     */
    void onReceivedGiftMsg(@NotNull  QGiftMsg giftMsg);
}
