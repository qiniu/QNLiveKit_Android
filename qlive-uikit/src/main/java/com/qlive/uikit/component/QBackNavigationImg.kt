package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.qlive.uikitcore.QComponent
import com.qlive.uikitcore.QKitImageView
import com.qlive.uikitcore.QUIKitContext
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

class QBackNavigationImg : androidx.appcompat.widget.AppCompatImageView, QComponent {
    override var kitContext: QUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {
            kitContext?.currentActivity?.finish()
        }
    }
}

class QBackRoomNavigationImg : QKitImageView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {
            kitContext?.currentActivity?.finish()
        }
    }
}