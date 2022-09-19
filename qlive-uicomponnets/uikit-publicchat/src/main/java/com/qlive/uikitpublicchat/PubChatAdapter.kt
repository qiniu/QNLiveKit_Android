package com.qlive.uikitpublicchat

import android.view.View
import com.bumptech.glide.Glide

import com.qlive.pubchatservice.QPublicChat
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.ext.toHtml
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import kotlinx.android.synthetic.main.kit_item_pubcaht.view.*

class PubChatAdapter : QSmartAdapter<QPublicChat>(R.layout.kit_item_pubcaht) {

    var mAvatarClickCall: (item: QPublicChat, view: View) -> Unit = { i, v -> }
    override fun convert(helper: QRecyclerViewHolder, item: QPublicChat) {
        Glide.with(mContext)
            .load(item.sendUser.avatar)
            .into(helper.itemView.ivAvatar)
        helper.itemView.tvName.text = item.sendUser.nick
        helper.itemView.tvContent.text = showHtml(item).toHtml() ?: ""
        helper.itemView.ivAvatar.setOnClickListener {
            mAvatarClickCall.invoke(item, it)
        }
    }

    private fun showHtml(mode: QPublicChat): String {
        return "<font color='#ffffff'>${mode.content}</font>"
    }

}