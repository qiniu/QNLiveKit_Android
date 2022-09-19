package com.qlive.rtclive

import com.qiniu.droid.rtc.QNAudioFrameListener
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.droid.rtc.QNVideoFrameType
import com.qlive.avparam.QVideoFrameListener

internal class BeautyVideoFrameListenerWarp(
    private val mVideoFrameListener: QNVideoFrameListener?,
    private val mInnerVideoFrameListener: QNVideoFrameListener?
) : QNVideoFrameListener {

    override fun onYUVFrameAvailable(
        p0: ByteArray?,
        p1: QNVideoFrameType?,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Long
    ) {
        mInnerVideoFrameListener?.onYUVFrameAvailable(p0, p1, p2, p3, p4, p5)
        mVideoFrameListener?.onYUVFrameAvailable(p0, p1, p2, p3, p4, p5)
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
        val textureId =
            mInnerVideoFrameListener?.onTextureFrameAvailable(p0, p1, p2, p3, p4, p5, p6)
                ?: p0
        return mVideoFrameListener?.onTextureFrameAvailable(
            textureId,
            p1,
            p2,
            p3,
            p4,
            p5,
            p6
        ) ?: textureId
    }
}