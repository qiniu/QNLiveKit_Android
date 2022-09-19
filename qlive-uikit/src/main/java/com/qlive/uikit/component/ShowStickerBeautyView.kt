package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.qlive.uikitcore.BeautyHook
import com.qlive.uikitcore.QKitImageView
import com.qlive.uikitcore.TestUIEvent
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

class ShowStickerBeautyView : QKitImageView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (BeautyHook.isEnable) {
            setDoubleCheckClickListener {
                kitContext?.getLiveFuncComponent(FuncCPTBeautyDialogShower::class.java)
                    ?.showBeautyStickDialog()
            }
            visibility = View.VISIBLE
        } else {
            visibility = View.GONE
        }
    }

    init {
        //ui通信事件测试
        registerEventAction(TestUIEvent::class.java) {
            Log.d("UIEvent", it.getAction() + " " + it.testInt)
        }
    }
}