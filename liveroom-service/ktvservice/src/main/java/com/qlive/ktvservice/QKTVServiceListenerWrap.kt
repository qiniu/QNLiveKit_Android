package com.qlive.ktvservice

import java.util.LinkedList

class QKTVServiceListenerWrap : QKTVServiceListener {

    private val mServiceListener = LinkedList<QKTVServiceListener>()

    fun addListener(listener: QKTVServiceListener) {
        mServiceListener.add(listener)
    }

    fun removeListener(listener: QKTVServiceListener) {
        mServiceListener.remove(listener)
    }

    override fun onError(errorCode: Int, msg: String) {
        mServiceListener.forEach {
            it.onError(errorCode,msg)
        }
    }

    override fun onStart(ktvMusic: QKTVMusic) {
        mServiceListener.forEach {
            it.onStart(ktvMusic)
        }
    }

    override fun onSwitchTrack(track: String) {
        mServiceListener.forEach {
            it.onSwitchTrack(track)
        }
    }

    override fun onPause() {
        mServiceListener.forEach {
            it.onPause()
        }
    }
    override fun onResume() {
        mServiceListener.forEach {
            it.onResume()
        }
    }

    override fun onStop() {
        mServiceListener.forEach {
            it.onStop()
        }
    }

    override fun updatePosition(position: Long, duration: Long) {
        mServiceListener.forEach {
            it.updatePosition(position, duration)
        }
    }

    override fun onPlayCompleted() {
        mServiceListener.forEach {
            it.onPlayCompleted()
        }
    }
}