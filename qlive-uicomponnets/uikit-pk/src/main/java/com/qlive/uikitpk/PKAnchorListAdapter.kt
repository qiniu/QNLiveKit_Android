package com.qlive.uikitpk

import com.bumptech.glide.Glide
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import kotlinx.android.synthetic.main.kit_item_pkable.view.*

class PKAnchorListAdapter :
    QSmartAdapter<QLiveRoomInfo>(R.layout.kit_item_pkable) {

    var inviteCall: (room: QLiveRoomInfo) -> Unit = {
    }

    override fun convert(holder: QRecyclerViewHolder, item: QLiveRoomInfo) {
        Glide.with(mContext).load(item.anchor.avatar)
            .into(holder.itemView.ivAvatar)
        holder.itemView.tvRoomName.text = item.title
        holder.itemView.tvAnchorName.text = item.anchor.nick
        holder.itemView.ivInvite.setOnClickListener {
            inviteCall.invoke(item)
        }
    }
}