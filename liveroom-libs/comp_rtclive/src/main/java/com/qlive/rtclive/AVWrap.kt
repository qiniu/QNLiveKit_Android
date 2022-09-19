package com.qlive.rtclive

import com.qiniu.droid.rtc.QNAudioFrameListener
import com.qiniu.droid.rtc.QNConnectionState
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.droid.rtc.QNVideoFrameType
import com.qlive.avparam.QAudioFrameListener
import com.qlive.avparam.QRoomConnectionState
import com.qlive.avparam.QVideoFrameListener
import com.qlive.avparam.QVideoFrameType
import java.nio.ByteBuffer

fun QNConnectionState.toQConnectionState(): QRoomConnectionState {
    return when (this) {
        QNConnectionState.DISCONNECTED -> QRoomConnectionState.DISCONNECTED
        QNConnectionState.CONNECTING -> QRoomConnectionState.CONNECTING
        QNConnectionState.CONNECTED -> QRoomConnectionState.CONNECTED
        QNConnectionState.RECONNECTING -> QRoomConnectionState.RECONNECTING
        QNConnectionState.RECONNECTED -> QRoomConnectionState.RECONNECTED
    }
}

fun QNVideoFrameType.toQVideoFrameType(): QVideoFrameType {
    return when (this) {
        QNVideoFrameType.YUV_NV21 -> QVideoFrameType.YUV_NV21
        QNVideoFrameType.TEXTURE_OES -> QVideoFrameType.TEXTURE_OES
        QNVideoFrameType.TEXTURE_RGB -> QVideoFrameType.TEXTURE_RGB
    }
}

class QVideoFrameListenerWrap(val frameListener: QVideoFrameListener?) : QNVideoFrameListener {
    override fun onYUVFrameAvailable(
        p0: ByteArray,
        p1: QNVideoFrameType,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Long
    ) {
        frameListener?.onYUVFrameAvailable(p0, p1.toQVideoFrameType(), p2, p3, p4, p5)
    }

    override fun onTextureFrameAvailable(
        p0: Int,
        p1: QNVideoFrameType,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Long,
        p6: FloatArray?
    ): Int {
        return frameListener?.onTextureFrameAvailable(
            p0,
            p1.toQVideoFrameType(),
            p2,
            p3,
            p4,
            p5,
            p6
        ) ?: p0
    }
}

class QAudioFrameListenerWrap(val frameListener: QAudioFrameListener?) :
    QNAudioFrameListener {
    override fun onAudioFrameAvailable(p0: ByteBuffer, p1: Int, p2: Int, p3: Int, p4: Int) {
        frameListener?.onAudioFrameAvailable(
            p0, p1, p2, p3, p4
        )
    }
}

