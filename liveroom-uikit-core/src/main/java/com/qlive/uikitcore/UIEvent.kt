package com.qlive.uikitcore

abstract class UIEvent() {
    fun getAction(): String {
        return this.javaClass.canonicalName.toString()
    }
    companion object {
        fun <T : UIEvent> getAction(eventClass: Class<T>): String {
            val classImpl = Class.forName(eventClass.canonicalName)
            val constructor = classImpl.getConstructor()
            val obj = constructor.newInstance() as UIEvent
            return obj.getAction()
        }
    }
}