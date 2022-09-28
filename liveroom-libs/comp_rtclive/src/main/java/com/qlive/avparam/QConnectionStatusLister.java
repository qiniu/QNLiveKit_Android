package com.qlive.avparam;

/**
 * rtc推流链接状态监听
 */
public interface QConnectionStatusLister {
    /**
     * rtc推流链接状态
     * @param state 状态枚举
     */
    void onConnectionStatusChanged(QRoomConnectionState state);
}
