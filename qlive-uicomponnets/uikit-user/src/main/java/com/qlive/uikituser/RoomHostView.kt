package com.qlive.uikituser

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.bumptech.glide.Glide
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.ext.toHtml
import kotlinx.android.synthetic.main.kit_view_room_host_slot.view.*

//房主信息左上角UI组件
class RoomHostView : QKitFrameLayout {

    // 静态配置
    companion object {
        //房主点击事件回调
        var onAnchorAvatarClickListener: (context: QLiveUIKitContext?, client: QLiveClient?, view: View, anchor: QLiveUser) -> Unit =
            { _, _, _, _ -> }

        //标题自定义显示回调 默认房间标题
        var showHostTitleCall: ((room: QLiveRoomInfo) -> String) = {
            "<font color='#ffffff'>" + it.title + "</font>"
        }

        // 副标题自定义回调 默认房间ID
        var showSubTitleCall: ((room: QLiveRoomInfo) -> String) = {
            "<font color='#ffffff'>" + it.anchor.nick + "</font>"
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getLayoutId(): Int {
        return R.layout.kit_view_room_host_slot;
    }

    override fun initView() {
        tvTitle.isSelected = true
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo,isResumeUIFromFloating)
        ivHost.setOnClickListener {
            onAnchorAvatarClickListener(kitContext, client, it, roomInfo.anchor)
        }
        tvTitle.text = showHostTitleCall.invoke(roomInfo).toHtml()
        tvSubTitle.text = showSubTitleCall.invoke(roomInfo).toHtml()
        Glide.with(context!!)
            .load(roomInfo.anchor.avatar)
            .into(ivHost)
    }

}

