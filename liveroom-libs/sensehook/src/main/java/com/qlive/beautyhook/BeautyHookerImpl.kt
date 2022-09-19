package com.qlive.beautyhook

import android.content.Context
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.sensetimeplugin.QNSenseTimePlugin
import com.qlive.avparam.QVideoFrameListener
import com.qlive.rtclive.BeautyHooker
import com.qlive.uiwidghtbeauty.ui.EffectBeautyDialog
import com.qlive.uiwidghtbeauty.QSenseTimeManager
import com.qlive.uiwidghtbeauty.ui.StickerDialog

class BeautyHookerImpl : BeautyHooker {

    private var mCurrentFrameListener: SenseVideoFrameListener? = null
    var mEffectBeautyDialog: EffectBeautyDialog? = null
        private set
    var mStickerDialog: StickerDialog? = null
        private set

    companion object {
        var senseTimePlugin: QNSenseTimePlugin? = null
        val addSubModels = ArrayList<String>()
        val addSubModelFromAssetsFiles = ArrayList<String>()
    }

    override fun init(context: Context) {
        QSenseTimeManager.initEffectFromLocalLicense(context, true)

        com.qlive.uikitcore.BeautyHook.showBeautyStickDialog = {
            mStickerDialog?.show(it, "")
        }
        com.qlive.uikitcore.BeautyHook.showBeautyEffectDialog = {
            mEffectBeautyDialog?.show(it, "")
        }
    }

    override fun provideVideoFrameListener(): QNVideoFrameListener {
        mCurrentFrameListener = SenseVideoFrameListener()
        return mCurrentFrameListener!!
    }

    override fun attach() {
        if (mEffectBeautyDialog == null) {
            mEffectBeautyDialog = EffectBeautyDialog()
        }
        if (mStickerDialog == null) {
            mStickerDialog = StickerDialog()
        }
    }

    override fun detach() {
        mStickerDialog = null
        mEffectBeautyDialog = null
        mCurrentFrameListener?.release()
    }

}