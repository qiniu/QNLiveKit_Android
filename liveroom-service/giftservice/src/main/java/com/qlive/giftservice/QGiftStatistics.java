package com.qlive.giftservice;

import java.io.Serializable;

/**
 * 送礼统计
 */
public class QGiftStatistics implements Serializable {
    public String bizID;
    public String userID;
    public int giftID;
    public int amount;
    public long createdAt;
}
