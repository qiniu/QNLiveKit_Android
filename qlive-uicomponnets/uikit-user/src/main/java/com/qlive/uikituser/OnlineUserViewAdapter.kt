package com.qlive.uikituser

import com.bumptech.glide.Glide
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikituser.databinding.KitItemOnlineUserBinding

class OnlineUserViewAdapter : QSmartViewBindAdapter<QLiveUser, KitItemOnlineUserBinding>() {
    override fun convertViewBindHolder(
        helper: QRecyclerViewBindHolder<KitItemOnlineUserBinding>,
        item: QLiveUser
    ) {
        Glide.with(mContext)
            .load(item.avatar)
            .into(helper.binding.ivAvatar)
    }
}