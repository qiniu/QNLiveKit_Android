package com.qlive.avparam

import android.view.Surface


interface QRenderCallback {
    fun onSurfaceCreated(var1: Surface, var2: Int, var3: Int)
    fun onSurfaceChanged(var1: Surface, var2: Int, var3: Int)
    fun onSurfaceDestroyed(var1: Surface)
}

enum class PreviewMode(val intValue: Int) {
    /**
      原始尺寸
     */
    ASPECT_RATIO_ORIGIN(0),

    /**
     * 适应屏幕
     */
    ASPECT_RATIO_FIT_PARENT(1),

    /**
     * 全屏铺满
     */
    ASPECT_RATIO_PAVED_PARENT(2),
    ASPECT_RATIO_16_9(3),
    ASPECT_RATIO_4_3(4),
}