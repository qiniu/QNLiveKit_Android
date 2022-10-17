package com.qlive.core.been;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 创建房间参数
 */
public class QCreateRoomParam implements Serializable {

    /**
     * 房间标题
     */
    public String title;
    /**
     * 房间公告
     */
    @SerializedName(value = "notice")
    public String notice;
    /**
     * 封面
     */
    @SerializedName(value = "cover_url")
    public String coverURL;
    /**
     * 扩展字段
     */
    @SerializedName(value = "extension")
    public HashMap<String, String> extension;

    /**
     * 非必须
     * 预计开播时间
     */
    @SerializedName(value = "start_at")
    public long startAt;
    /**
     * 非必须
     * 预计结束时间
     */
    @SerializedName(value = "end_at")
    public long endAt;
    /**
     * 非必须
     * 推流token 过期时间
     */
    @SerializedName(value = "publish_expire_at")
    public long publishExpireAt;

}
