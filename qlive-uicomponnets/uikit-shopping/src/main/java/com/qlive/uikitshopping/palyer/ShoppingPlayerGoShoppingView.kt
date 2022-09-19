package com.qlive.uikitshopping.palyer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.QRoomComponent
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitshopping.PlayerShoppingDialog

@SuppressLint("AppCompatCustomView")
class ShoppingPlayerGoShoppingView : ImageView, QRoomComponent {
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {
            PlayerShoppingDialog(kitContext!!, client!!).show(kitContext!!.fragmentManager, "")
        }
    }
}