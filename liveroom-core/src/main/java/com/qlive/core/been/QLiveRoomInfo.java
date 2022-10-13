package com.qlive.core.been;

import com.google.gson.annotations.SerializedName;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Map;

/**
 * 房间信息
 */
public class QLiveRoomInfo implements Serializable, Cloneable {

    /**
     * 房间ID
     */
    @SerializedName(value = "live_id")
    public String liveID;

    /**
     * 房间标题
     */
    @SerializedName(value = "title")
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
    public Map<String, String> extension;

    /**
     * 主播信息
     */
    @SerializedName(value = "anchor_info")
    public QLiveUser anchor;

    @SerializedName(value = "room_token")
    public String roomToken;

    /**
     * 当前房间的pk会话信息
     */
    @SerializedName(value = "pk_id")
    public String pkID;

    /**
     * 在线人数
     */
    @SerializedName(value = "online_count")
    public long onlineCount;

    /**
     * 开始时间
     */
    @SerializedName(value = "start_time")
    public long startTime;

    /**
     * 结束时间
     */
    @SerializedName(value = "end_time")
    public long endTime;

    /**
     * 聊天室ID
     */
    @SerializedName(value = "chat_id")
    public String chatID;

    /**
     * 推流地址
     */
    @SerializedName(value = "push_url")
    public String pushURL;

    /**
     * 拉流地址
     */
    @SerializedName(value = "hls_url")
    public String hlsURL;
    /**
     * 拉流地址
     */
    @SerializedName(value = "rtmp_url")
    public String rtmpURL;
    /**
     * 拉流地址
     */
    @SerializedName(value = "flv_url")
    public String flvURL;
    /**
     * pv
     */
    @SerializedName(value = "pv")
    public Double pv;
    /**
     * uv
     */
    @SerializedName(value = "uv")
    public Double uv;
    /**
     * 总人数
     */
    @SerializedName(value = "total_count")
    public int totalCount;

    /**
     * 连麦者数量
     */
    @SerializedName(value = "total_mics")
    public int totalMics;

    /**
     * 直播间状态
     */
    @SerializedName(value = "live_status")
    public int liveStatus;

    /**
     * 主播在线状态
     */
    @SerializedName(value = "AnchorStatus")
    public int anchorStatus;


    @Override
    public QLiveRoomInfo clone() {
        try {
            QLiveRoomInfo clone = (QLiveRoomInfo) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}
