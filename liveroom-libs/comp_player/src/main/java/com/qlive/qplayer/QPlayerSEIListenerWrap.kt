package com.qlive.qplayer

import com.qlive.avparam.QPlayerSEIListener
import java.util.LinkedList

class QPlayerSEIListenerWrap : QPlayerSEIListener {

    private val mQPlayerSEIListeners = LinkedList<QPlayerSEIListener>()

    fun addSEIListener(listener: QPlayerSEIListener) {
        mQPlayerSEIListeners.add(listener)
    }

    fun removeSEIListener(listener: QPlayerSEIListener) {
        mQPlayerSEIListeners.remove(listener)
    }

    fun clear() {
        mQPlayerSEIListeners.clear()
    }

    override fun onSEI(sei:String) {
        mQPlayerSEIListeners.forEach {
            it.onSEI(sei)
        }
    }
}