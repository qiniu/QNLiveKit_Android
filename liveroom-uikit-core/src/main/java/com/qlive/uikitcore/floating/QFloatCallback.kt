package com.qlive.uikitcore.floating

import android.view.MotionEvent
import android.view.View

interface QFloatCallback {

    fun onCreate(view: View) {}
    fun onShow(view: View) {}

    fun onHide(view: View) {}

    fun onDismiss() {}

    /**
     * 触摸事件的回调
     */
    fun onTouchEvent(view: View, event: MotionEvent) {}

    /**
     * 浮窗被拖拽时的回调，坐标为浮窗的左上角坐标
     */
    fun onDrag(view: View, event: MotionEvent) {}

    /**
     * 拖拽结束时的回调，坐标为浮窗的左上角坐标
     */
    fun onDragEnd(view: View) {}
}