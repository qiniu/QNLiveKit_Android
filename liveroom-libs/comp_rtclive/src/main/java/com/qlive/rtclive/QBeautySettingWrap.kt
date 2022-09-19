package com.qlive.rtclive

import com.qiniu.droid.rtc.QNBeautySetting
import com.qlive.avparam.QBeautySetting

fun QBeautySetting.toQNBeautySetting(): QNBeautySetting {
    val setting = QNBeautySetting(smoothLevel, whiten, redden);
    setting.setEnable(isEnabled)
    return setting
}