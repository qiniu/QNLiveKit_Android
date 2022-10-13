package com.qlive.giftservice;

import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;

/**
 * 礼物服务
 */
public interface QGiftService extends QLiveService {
    /**
     * 发礼物
     *
     * @param giftID
     * @param amount
     * @param callback
     */
    void sendGift(int giftID, int amount,QLiveCallBack<Void> callback);

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
