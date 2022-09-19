package com.qlive.core;

/**
 * 直播状态枚举
 */
public enum QLiveStatus {
    /**
     * 房间已创建
     */
    PREPARE(),
    /**
     * 房间已发布
     */
    ON(),
    /**
     * 主播上线
     */
    ANCHOR_ONLINE(),
    /**
     * 主播已离线
     */
    ANCHOR_OFFLINE(),
    /**
     * 房间已关闭
     */
    OFF()
}
