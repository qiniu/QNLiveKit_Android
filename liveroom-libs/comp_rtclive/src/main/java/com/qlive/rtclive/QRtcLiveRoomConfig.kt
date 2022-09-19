package com.qlive.rtclive

import com.qiniu.droid.rtc.QNRTCSetting

/**
 *rtc配置
 */
object QRtcLiveRoomConfig {

    /**
     * rtc配置
     */
    var mRTCSettingGetter: () -> QNRTCSetting = {
        QNRTCSetting().apply {
            isMaintainResolution = true
            isHWCodecEnabled = true
        }
    }
}