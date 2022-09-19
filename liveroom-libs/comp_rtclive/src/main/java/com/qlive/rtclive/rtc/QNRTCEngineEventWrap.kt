package com.qlive.rtclive.rtc

import com.qiniu.droid.rtc.*
import com.qlive.liblog.QLiveLogUtil
import java.util.*


/**
 * 引擎监听包装 用于让各个页面都能处理监听并且只处理自己关心的流程
 */
class QNRTCEngineEventWrap : ExtQNClientEventListener {

    private val TAG = "QNRTCEngineEventWrap"
    private val extraQNRTCEngineEventListeners: LinkedList<QNClientEventListener> = LinkedList()

    fun addExtraQNRTCEngineEventListener(
        extraQNRTCEngineEventListener: QNClientEventListener,
        toHead: Boolean = false
    ) {

        if (toHead) {
            extraQNRTCEngineEventListeners.addFirst(extraQNRTCEngineEventListener)
        } else {
            extraQNRTCEngineEventListeners.add(extraQNRTCEngineEventListener)
        }
    }


    fun removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: QNClientEventListener) {
        extraQNRTCEngineEventListeners.remove(extraQNRTCEngineEventListener)
    }

    fun clear() {
        extraQNRTCEngineEventListeners.clear()
    }

    override fun onConnectionStateChanged(
        p0: QNConnectionState?,
        p1: QNConnectionDisconnectedInfo?
    ) {
        QLiveLogUtil.d("onConnectionStateChanged  ${p0?.name}")
        val iterator = extraQNRTCEngineEventListeners.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            item.onConnectionStateChanged(p0, p1)
        }
    }

    override fun onUserJoined(p0: String?, p1: String?) {
        QLiveLogUtil.d("onUserJoined  ${p0} ${p1}")
        extraQNRTCEngineEventListeners.forEach {
            it.onUserJoined(p0, p1)
        }
    }

    override fun onUserReconnecting(p0: String?) {
        extraQNRTCEngineEventListeners.forEach {
            it.onUserReconnecting(p0)
        }
    }

    override fun onUserReconnected(p0: String?) {
        extraQNRTCEngineEventListeners.forEach {
            it.onUserReconnected(p0)
        }
    }

    override fun onUserLeft(p0: String?) {
        QLiveLogUtil.d("onUserLeft  ${p0}")
        extraQNRTCEngineEventListeners.forEach {
            it.onUserLeft(p0)
        }
    }

    override fun onUserPublished(p0: String?, p1: MutableList<QNRemoteTrack>?) {
        QLiveLogUtil.d("onUserPublished  ${p0} ${p1?.size}")
        extraQNRTCEngineEventListeners.forEach {
            it.onUserPublished(p0, p1)
        }
    }

    override fun onUserUnpublished(p0: String?, p1: MutableList<QNRemoteTrack>?) {
        QLiveLogUtil.d("onUserPublished  ${p0} ${p1?.size}")
        extraQNRTCEngineEventListeners.forEach {
            it.onUserUnpublished(p0, p1)
        }
    }

    override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
        QLiveLogUtil.d("onLocalPublished  ${var2.size}")
        extraQNRTCEngineEventListeners.forEach {
            if (it is ExtQNClientEventListener) {
                it.onLocalPublished(var1, var2)
            }
        }
    }

    override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
        QLiveLogUtil.d("onLocalUnpublished   ${var2.size}")
        extraQNRTCEngineEventListeners.forEach {
            if (it is ExtQNClientEventListener) {
                it.onLocalUnpublished(var1, var2)
            }
        }
    }

    override fun onSubscribed(
        p0: String?,
        p1: MutableList<QNRemoteAudioTrack>?,
        p2: MutableList<QNRemoteVideoTrack>?
    ) {
        extraQNRTCEngineEventListeners.forEach {
            it.onSubscribed(p0, p1, p2)
        }
    }

    override fun onMessageReceived(p0: QNCustomMessage?) {
        extraQNRTCEngineEventListeners.forEach {
            it.onMessageReceived(p0)
        }
    }

    override fun onMediaRelayStateChanged(p0: String, p1: QNMediaRelayState) {
        QLiveLogUtil.d( " onMediaRelayStateChanged  ${p0} ${p1.name}")
        extraQNRTCEngineEventListeners.forEach {
            it.onMediaRelayStateChanged(p0, p1)
        }
    }
}