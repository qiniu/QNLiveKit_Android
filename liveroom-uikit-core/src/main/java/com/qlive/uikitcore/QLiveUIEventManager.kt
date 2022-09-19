package com.qlive.uikitcore

import java.util.*

class QLiveUIEventManager {

    var mQLiveComponents: () -> List<BaseComponent<*>> = {
        LinkedList<BaseComponent<*>>()
    }

    val mActionMap = HashMap<BaseComponent<*>, HashMap<String, Function1<UIEvent, Unit>>>()
    private fun getActionMap(component: BaseComponent<*>): HashMap<String, Function1<UIEvent, Unit>> {
        var map = mActionMap.get(component)
        if (map == null) {
            map = HashMap<String, Function1<UIEvent, Unit>>()
            mActionMap.put(component, map)
        }
        return map
    }

    /**
     * 发送UI事件给所有UI组件
     *
     * @param action 事件名字
     * @param data   数据
     */
    fun <T : UIEvent?> sendUIEvent(event: T) {
        mQLiveComponents().forEach {
            val function1 = getActionMap(it)[event!!.getAction()]
            function1?.invoke(event)
        }
    }

    fun clear() {
        mActionMap.clear()
    }
}