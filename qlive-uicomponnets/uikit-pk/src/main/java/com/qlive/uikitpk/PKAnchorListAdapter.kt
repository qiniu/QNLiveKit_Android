package com.qlive.uikitpk

import com.bumptech.glide.Glide
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitpk.databinding.KitItemPkableBinding

class PKAnchorListAdapter :
    QSmartViewBindAdapter<QLiveRoomInfo,KitItemPkableBinding>() {

    var inviteCall: (room: QLiveRoomInfo) -> Unit = {
    }

    override fun convertViewBindHolder(
        helper: QRecyclerViewBindHolder<KitItemPkableBinding>,
        item: QLiveRoomInfo
    ) {
        Glide.with(mContext).load(item.anchor.avatar)
            .into(helper.binding.ivAvatar)
        helper.binding.tvRoomName.text = item.title
        helper.binding.tvAnchorName.text = item.anchor.nick
        helper.binding.ivInvite.setOnClickListener {
            inviteCall.invoke(item)
        }
    }
}