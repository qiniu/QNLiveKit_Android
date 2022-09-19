package com.qlive.beautyhook

import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.droid.rtc.QNVideoFrameType

class SenseVideoFrameListener : QNVideoFrameListener {
    override fun onYUVFrameAvailable(
        p0: ByteArray?,
        p1: QNVideoFrameType?,
        p2: Int,
        p3: Int,
        p4: Int,
        p5: Long
    ) {
    }

    private var isInit = false
    private var mRotation = -1
    private fun checkInit(): Boolean {
        if (BeautyHookerImpl.senseTimePlugin != null
        ) {
            if (!isInit) {
                BeautyHookerImpl.senseTimePlugin!!.init()
                BeautyHookerImpl.addSubModels.forEach {
                    BeautyHookerImpl.senseTimePlugin!!.addSubModel(it)
                }
                BeautyHookerImpl.addSubModelFromAssetsFiles.forEach {
                    BeautyHookerImpl.senseTimePlugin!!.addSubModelFromAssetsFile(it)
                }
                // BeautyHookerImpl.senseTimePlugin!!.recoverEffects()
                isInit = true
            }
            return true
        } else {
            return false
        }
    }

    private var lastTextureID: Int = -1
    override fun onTextureFrameAvailable(
        textureID: Int,
        type: QNVideoFrameType,
        width: Int,
        height: Int,
        rotation: Int,
        timestampNs: Long,
        transformMatrix: FloatArray?
    ): Int {
        if (isInit) {
            if (lastTextureID != textureID) {
                release()
            }
        }
        lastTextureID = textureID
        return if (checkInit()) {
            if (mRotation != rotation) {
                BeautyHookerImpl.senseTimePlugin?.updateDirection(
                    rotation,
                    (rotation == 270 || rotation == 90),
                    false
                )
                mRotation = rotation
            }
            BeautyHookerImpl.senseTimePlugin!!.processTexture(
                textureID,
                width,
                height
            )
        } else {
            textureID;
        }
    }

    fun release() {
        isInit = false
        BeautyHookerImpl.senseTimePlugin?.destroy()
    }
}