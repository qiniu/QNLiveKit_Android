package com.qlive.core;

/**
 * 直播状态监听
 */
public interface QLiveStatusListener {
    /**
     * 直播间状态变化 业务状态
     *
     * @param liveStatus 业务状态
     */
    void onLiveStatusChanged(QLiveStatus liveStatus, String msg);

}
