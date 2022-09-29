package com.qlive.likeservice;

import com.google.gson.annotations.SerializedName;
import com.qlive.core.been.QLiveUser;

import java.io.Serializable;

/**
 * 点赞
 */
public class QLike implements Serializable {
    /**
     * 直播间ID
     */
    public String liveID;
    /**
     * 点赞数量
     */
    public int count;
    /**
     * 点赞者
     */
    public QLiveUser sender;
}