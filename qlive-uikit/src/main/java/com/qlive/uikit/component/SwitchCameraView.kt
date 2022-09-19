package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import com.qlive.pushclient.QPusherClient
import com.qlive.uikitcore.QKitTextView
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

class SwitchCameraView : QKitTextView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {
            (client as QPusherClient).switchCamera(null)
        }
    }
}