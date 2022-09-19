package com.qlive.uikit.component

import android.content.Context
import android.view.KeyEvent
import com.qlive.roomservice.QRoomService
import com.qlive.uikitcore.QLiveFuncComponent

abstract class OnKeyDownMonitor(context: Context) : QLiveFuncComponent(context, null) {
    abstract fun onActivityKeyDown(keyCode: Int, event: KeyEvent): Boolean
}

/**
 * activity默认事件虚拟拦截 功能组件
 * @param context
 */
class FuncCPTDefaultKeyDownMonitor(context: Context) :OnKeyDownMonitor(context){
    override fun onActivityKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK
            && client?.getService(QRoomService::class.java)?.roomInfo != null
        ) {
            return true
        }
        return false
    }
}
