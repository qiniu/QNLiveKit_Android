package com.qlive.giftservice;

import com.qlive.core.QLiveCallBack;

/**
 * 礼物服务
 */
public interface QGiftService {
    /**
     * 发礼物
     *
     * @param giftID
     * @param amount
     * @param redo
     * @param callback
     */
    void sendGift(int giftID, int amount, boolean redo, QLiveCallBack<Void> callback);

    /**
     * 添加礼物监听
     *
     * @param listener
     */
    void addGiftServiceListener(QGiftServiceListener listener);

    /**
     * 移除礼物监听
     *
     * @param listener
     */
    void removeGiftServiceListener(QGiftServiceListener listener);

}
