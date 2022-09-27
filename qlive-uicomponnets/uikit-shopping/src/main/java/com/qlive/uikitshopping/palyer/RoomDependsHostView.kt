package com.qlive.uikitshopping.palyer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.QRoomComponent
import com.qlive.uikitcore.ext.toHtml
import com.qlive.uikitshopping.databinding.KitShopingpayerRoomHostBinding

class RoomDependsHostView : QRoomComponent, FrameLayout {

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

    private lateinit var binding: KitShopingpayerRoomHostBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = KitShopingpayerRoomHostBinding.inflate(LayoutInflater.from(context), this, true)
        binding.tvTitle.isSelected = true
    }

    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null
    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)
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
