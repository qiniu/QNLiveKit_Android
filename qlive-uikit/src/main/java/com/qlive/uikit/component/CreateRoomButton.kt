package com.qlive.uikit.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.sdk.QLive
import com.qlive.uikit.RoomPage
import com.qlive.uikitcore.QUIKitContext
import com.qlive.uikitcore.QComponent
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

/**
 * 创建房间按钮
 */
@SuppressLint("AppCompatCustomView")
class CreateRoomButton : FrameLayout, QComponent {
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
            QLive.getLiveUIKit().getPage(RoomPage::class.java)
                .startAnchorRoomWithPreview(context, object : QLiveCallBack<QLiveRoomInfo> {
                    override fun onError(code: Int, msg: String?) {
                        msg?.asToast(kitContext?.androidContext)
                    }
                    override fun onSuccess(data: QLiveRoomInfo?) {}
                })
        }
    }
}