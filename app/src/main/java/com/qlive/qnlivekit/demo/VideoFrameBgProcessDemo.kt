package com.qlive.qnlivekit.demo

import android.content.Context
import android.util.AttributeSet
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
class VideoFrameBgProcessDemo : QLiveFuncComponent {

    var videoW = 100
    var videoH = 100
    var videoX = 0
    var videoY = 0
    var bgImg = -1

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
            if (bgImg == -1) {
                return textureID
            }
            if (mTextureRenderer?.w != width || mTextureRenderer?.h != height) {
                mTextureRenderer?.detach()
                mTextureRenderer = TextureRenderer()
                mTextureRenderer?.setTargetSize(videoW, videoH, videoX, videoY)
                mTextureRenderer?.attach(width, height)
            }
            if (!mTextureUtils.isInit) {
                mTextureUtils.init()
                mTextureUtils.loadTexture(context, bgImg, null)
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
    ) {
        val styled =
            context.obtainStyledAttributes(attrs, R.styleable.VideoFrameBgProcess, defStyleAttr, 0)
        videoW = styled.getInteger(R.styleable.VideoFrameBgProcess_video_width, 100)
        videoH = styled.getInteger(R.styleable.VideoFrameBgProcess_video_height, 100)
        videoX = styled.getInteger(R.styleable.VideoFrameBgProcess_video_x, 100)
        videoY = styled.getInteger(R.styleable.VideoFrameBgProcess_video_y, 100)
        bgImg = styled.getResourceId(R.styleable.VideoFrameBgProcess_bgImg, -1)
        styled.recycle()
    }

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