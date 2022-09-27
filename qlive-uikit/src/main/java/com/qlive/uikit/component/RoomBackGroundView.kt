package com.qlive.uikit.component
import android.content.Context
import android.util.AttributeSet
import com.qlive.uikitcore.*

/**
 * 房间背景图片组件
 */
class RoomBackGroundView : QKitImageView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}