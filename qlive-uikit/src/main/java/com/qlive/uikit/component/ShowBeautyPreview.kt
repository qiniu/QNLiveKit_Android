package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import com.qlive.avparam.QBeautySetting
import com.qlive.pushclient.QPusherClient
import com.qlive.uikitcore.BeautyHook
import com.qlive.uikitcore.QKitTextView
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

class ShowBeautyPreview : QKitTextView {

    private var isOpen = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (BeautyHook.isEnable) {
            setDoubleCheckClickListener {
                kitContext?.getLiveFuncComponent(FuncCPTBeautyDialogShower::class.java)?.showBeautyEffectDialog()
            }
        } else {
            setOnClickListener {
                if (!isOpen) {
                    isOpen = true
                    (client as QPusherClient).setDefaultBeauty(
                        QBeautySetting(
                            0.6f,
                            0.8f,
                            0.6f
                        ).apply { setEnable(true) })
                } else {
                    isOpen = false
                    (client as QPusherClient).setDefaultBeauty(
                        QBeautySetting(
                            0f,
                            0f,
                            0f
                        ).apply { setEnable(false) })
                }
            }
        }
    }
}