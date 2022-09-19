package com.qlive.uikitshopping.palyer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.QRoomComponent
import com.qlive.uikitcore.ext.toHtml

@SuppressLint("AppCompatCustomView")
class RoomDependsIdView : TextView, QRoomComponent {
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null
    companion object{
        //文本显示
        var getShowTextCall: ((roomInfo: QLiveRoomInfo) -> String) = { info ->
            "<font color='#ffffff'>${info.liveID}</font>"
        }
    }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)
        text = getShowTextCall.invoke(roomInfo).toHtml()
    }
}