package com.qlive.uikituser

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.bumptech.glide.Glide
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.QKitViewBindingCardView
import com.qlive.uikitcore.QKitViewBindingFrameLayout
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.ext.toHtml
import com.qlive.uikituser.databinding.KitViewRoomHostSlotBinding

//房主信息左上角UI组件
class RoomHostView : QKitViewBindingFrameLayout<KitViewRoomHostSlotBinding> {

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

    override fun initView() {
        binding.tvTitle.isSelected = true
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        binding.ivHost.setOnClickListener {
            onAnchorAvatarClickListener(kitContext, client, it, roomInfo.anchor)
        }
        binding.tvTitle.text = showHostTitleCall.invoke(roomInfo).toHtml()
        binding.tvSubTitle.text = showSubTitleCall.invoke(roomInfo).toHtml()
        Glide.with(context!!)
            .load(roomInfo.anchor.avatar)
            .into(binding.ivHost)
    }

}

