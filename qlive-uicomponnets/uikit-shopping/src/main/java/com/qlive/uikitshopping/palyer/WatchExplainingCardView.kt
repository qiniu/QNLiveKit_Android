package com.qlive.uikitshopping.palyer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.shoppingservice.QItem
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.QRoomComponent
import com.qlive.uikitshopping.WatchExplainingPage
import com.qlive.uikitshopping.databinding.KitViewExplainingQitemBinding

class WatchExplainingCardView : CardView, QRoomComponent {

    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null

    private val binding: KitViewExplainingQitemBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = KitViewExplainingQitemBinding.inflate(LayoutInflater.from(context), this, true)
        binding.ivClose.setOnClickListener {
            visibility = View.INVISIBLE
        }
    }

    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)

        val item: QItem = (kitContext?.getIntent()
            ?.getSerializableExtra(WatchExplainingPage.params_key_item) as QItem?)
            ?: return
        Glide.with(kitContext!!.androidContext)
            .load(item.thumbnail)
            .into(binding.ivCover)
        binding.tvNowPrice.text = item.currentPrice
        binding.tvTitle.text = item.title
        binding.tvOrder.text = item.order.toString()
        visibility = View.VISIBLE
    }
}