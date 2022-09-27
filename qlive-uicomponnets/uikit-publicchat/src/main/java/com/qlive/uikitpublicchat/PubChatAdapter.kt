package com.qlive.uikitpublicchat

import android.view.View
import com.bumptech.glide.Glide

import com.qlive.pubchatservice.QPublicChat
import com.qlive.uikitcore.adapter.QRecyclerViewBindAdapter
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.ext.toHtml
import com.qlive.uikitpublicchat.databinding.KitItemPubcahtBinding

class PubChatAdapter : QRecyclerViewBindAdapter<QPublicChat, KitItemPubcahtBinding>() {

    var mAvatarClickCall: (item: QPublicChat, view: View) -> Unit = { i, v -> }

    private fun showHtml(mode: QPublicChat): String {
        return "<font color='#ffffff'>${mode.content}</font>"
    }

    override fun convertViewBindHolder(
        helper: QRecyclerViewBindHolder<KitItemPubcahtBinding>,
        item: QPublicChat
    ) {
        Glide.with(mContext)
            .load(item.sendUser.avatar)
            .into(helper.binding.ivAvatar)
        helper.binding.tvName.text = item.sendUser.nick
        helper.binding.tvContent.text = showHtml(item).toHtml() ?: ""
        helper.binding.ivAvatar.setOnClickListener {
            mAvatarClickCall.invoke(item, it)
        }
    }
}