package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.qlive.uikit.R
import com.qlive.uikitcore.QUIKitContext
import com.qlive.uikitcore.QComponent

/**
 * 房间列表toolbar
 */
class RoomListToolbar : FrameLayout, QComponent {
    override var kitContext: QUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_room_list_toobar, this, true)
        findViewById<View>(R.id.tvBack).setOnClickListener {
            kitContext?.currentActivity?.finish()
        }
    }
}