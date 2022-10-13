package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.qlive.sdk.QLive
import com.qlive.uikit.LiveRecordPage
import com.qlive.uikitcore.QComponent
import com.qlive.uikitcore.QUIKitContext
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

class LiveRecordButton : FrameLayout, QComponent {
    override var kitContext: QUIKitContext? = null
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {
            //调用开始创建房间方法
            QLive.getLiveUIKit().getPage(LiveRecordPage::class.java).start(context)
        }
    }
}