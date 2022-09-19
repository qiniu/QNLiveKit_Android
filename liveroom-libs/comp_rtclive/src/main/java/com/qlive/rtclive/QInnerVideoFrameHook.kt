package com.qlive.rtclive

import android.content.Context
import com.qiniu.droid.rtc.QNVideoFrameListener

object QInnerVideoFrameHook {
    var mBeautyHooker: BeautyHooker? = null
    var isEnable = false
    fun checkHasHooker(): Boolean {
        try {
            val classStr = "com.qlive.beautyhook.BeautyHookerImpl"
            val classImpl = Class.forName(classStr)
            val constructor = classImpl.getConstructor()
            val obj = constructor.newInstance() as BeautyHooker
            QInnerVideoFrameHook.mBeautyHooker = obj
            isEnable = true
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            isEnable = false
        }
        return isEnable
    }
}

interface BeautyHooker {
    fun init(context: Context)
    fun provideVideoFrameListener(): QNVideoFrameListener
    fun attach()
    fun detach()
}
