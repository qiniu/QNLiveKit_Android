package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.qlive.roomservice.QRoomService
import com.qlive.uikitcore.QLiveFuncComponent

abstract class OnKeyDownMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    abstract fun onActivityKeyDown(keyCode: Int, event: KeyEvent): Boolean
}

/**
 * activity默认事件虚拟拦截 功能组件
 * @param context
 */
class FuncCPTDefaultKeyDownMonitor :OnKeyDownMonitor{
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    override fun onActivityKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && client?.getService(QRoomService::class.java)?.roomInfo != null
        ) {
            return true
        }
        return false
    }
}
