package com.qlive.likeservice;

import java.io.Serializable;

/**
 * 点赞响应
 */
public class QLikeResponse implements Serializable {
    /**
     * 直播间总点赞数
     */
    public int total;
    /**
     * 我在直播间内的总点赞数
     */
    public int count;
}
