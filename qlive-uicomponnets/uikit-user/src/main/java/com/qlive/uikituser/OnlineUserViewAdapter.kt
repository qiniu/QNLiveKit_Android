package com.qlive.uikituser

import com.bumptech.glide.Glide
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import kotlinx.android.synthetic.main.kit_item_online_user.view.*

class OnlineUserViewAdapter : QSmartAdapter<QLiveUser>(
    R.layout.kit_item_online_user
) {
    override fun convert(helper: QRecyclerViewHolder, item: QLiveUser) {
        Glide.with(mContext)
            .load(item.avatar)
            .into(helper.itemView.ivAvatar)
    }
}