package com.qlive.uikitshopping

import android.content.Context
import android.util.AttributeSet
import com.qlive.core.QClientType
import com.qlive.uikitcore.QKitImageView

/**
 * 购物车icon
 *
 * @constructor Create empty Go shopping img view
 */
class GoShoppingImgView : QKitImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setOnClickListener {
            client ?: return@setOnClickListener
            if (client!!.clientType == QClientType.PUSHER) {
                AnchorShoppingDialog(kitContext!!,client!!).show(kitContext!!.fragmentManager,"")
            } else {
                PlayerShoppingDialog(kitContext!!,client!!).show(kitContext!!.fragmentManager,"")
            }
        }
    }
}