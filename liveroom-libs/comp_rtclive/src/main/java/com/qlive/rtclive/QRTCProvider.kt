package com.qlive.rtclive

/**
 * 依赖rtc 的插件
 */
interface QRTCProvider {
    /**
     * 获得rtc对象
     */
    var rtcRoomGetter: (() -> QRtcLiveRoom)
}