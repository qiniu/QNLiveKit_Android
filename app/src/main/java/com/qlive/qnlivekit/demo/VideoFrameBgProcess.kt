package com.qlive.qnlivekit.demo

import android.content.Context
import android.util.AttributeSet
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.droid.rtc.QNVideoFrameType
import com.qlive.avparam.QVideoFrameListener
import com.qlive.avparam.QVideoFrameType
import com.qlive.core.QLiveClient
import com.qlive.pushclient.QPusherClient
import com.qlive.qnlivekit.R
import com.qlive.qnlivekit.demo.fbo.TextureRenderer
import com.qlive.qnlivekit.demo.fbo.TextureUtils
import com.qlive.uikitcore.QLiveFuncComponent

/**
 * Video frame bg process
 *
 * @constructor Create empty Video frame bg process
 */
class VideoFrameBgProcess : QLiveFuncComponent {

    private var mTextureRenderer: TextureRenderer? = null
    private var mTextureUtils = TextureUtils()
    val mRotationMatrix: FloatArray = arrayOf<Float>(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f,
    ).toFloatArray()

    private val frameFrameListener = object : QVideoFrameListener {
        override fun onTextureFrameAvailable(
            textureID: Int,
            type: QVideoFrameType?,
            width: Int,
            height: Int,
            rotation: Int,
            timestampNs: Long,
            transformMatrix: FloatArray?
        ): Int {
            if (mTextureRenderer?.w != width || mTextureRenderer?.h != height) {
                mTextureRenderer?.detach()
                mTextureRenderer = TextureRenderer()
                mTextureRenderer?.setTargetSize(320, 330, 0, 0)
                mTextureRenderer?.attach(width, height)
            }
            if (!mTextureUtils.isInit) {
                mTextureUtils.init()
                mTextureUtils.loadTexture(context, R.drawable.ic_goods_demo_des, null)
            }

            return mTextureRenderer!!.drawFrame(
                mTextureUtils.textureIds[0],
                textureID,
                transformMatrix ?: mRotationMatrix, rotation
            )
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        if (client is QPusherClient) {
            client.setVideoFrameListener(frameFrameListener)
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mTextureUtils.deInnit()
        mTextureRenderer?.detach()
    }

}