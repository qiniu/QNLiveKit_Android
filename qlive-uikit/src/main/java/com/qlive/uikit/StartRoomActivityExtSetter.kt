package com.qlive.uikit

import android.content.Intent

/**
 * 启动activity 附加本地参数到Intent
 */
interface StartRoomActivityExtSetter {
    fun setExtParams(startIntent: Intent)
}