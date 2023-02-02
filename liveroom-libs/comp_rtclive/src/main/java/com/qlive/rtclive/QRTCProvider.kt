package com.qlive.rtclive

/**
 * rtc 提供者
 */
interface QRTCProvider {
    /**
     * 获得rtc对象
     */
    var rtcRoomGetter: (() -> QRtcLiveRoom)
}