package com.qlive.uikitpublicchat

import android.view.View
import com.bumptech.glide.Glide
import com.qlive.giftservice.QGiftMsg.GIFT_ACTION

import com.qlive.pubchatservice.QPublicChat
import com.qlive.uikitcore.adapter.*
import com.qlive.uikitcore.ext.toHtml
import com.qlive.uikitcore.smartrecycler.QSmartMultipleAdapter
import com.qlive.uikitpublicchat.databinding.KitItemPubchatBinding
import com.qlive.uikitpublicchat.databinding.KitItemPubchatGiftBinding

val ViewTypeCommon = 1
val ViewTypeGift = 2

fun QPublicChat.getType(): Int {
    if (action == GIFT_ACTION) {
        return ViewTypeGift
    } else {
        return ViewTypeCommon
    }
}

class PubChatAdapter : QSmartMultipleAdapter<QPublicChat>(ArrayList<QPublicChat>()) {
    var mAvatarClickCall: (item: QPublicChat, view: View) -> Unit = { i, v -> }
    override fun getViewType(t: QPublicChat): Int {
        return t.getType()
    }

    override fun registerItemProvider() {
        itemProvider[ViewTypeCommon] = CommonItemProvider(this)
        itemProvider[ViewTypeGift] = GiftItemProvider(this)
    }

    fun showHtml(mode: QPublicChat): String {
        return if (mode.getType() == ViewTypeGift) {
            "<font color='#CCDFFC'>${mode.sendUser.nick}</font><font color='#ffffff'> 打赏 </font><font color='#F3CF22'>${mode.content}</font>"
        } else {
            "<font color='#ffffff'>${mode.content}</font>"
        }
    }

    class CommonItemProvider(private val pubChatAdapter: PubChatAdapter) :
        ViewBindingItemProvider<QPublicChat, KitItemPubchatBinding>() {

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemPubchatBinding>,
            data: QPublicChat,
            position: Int
        ) {
            Glide.with(pubChatAdapter.mContext)
                .load(data.sendUser?.avatar?:"")
                .into(helper.binding.ivAvatar)
            helper.binding.tvName.text = data.sendUser.nick
            helper.binding.tvContent.text = pubChatAdapter.showHtml(data).toHtml() ?: ""
            helper.binding.ivAvatar.setOnClickListener {
                pubChatAdapter.mAvatarClickCall.invoke(data, it)
            }
        }
    }

    class GiftItemProvider(private val pubChatAdapter: PubChatAdapter) :
        ViewBindingItemProvider<QPublicChat, KitItemPubchatGiftBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemPubchatGiftBinding>,
            data: QPublicChat,
            position: Int
        ) {
            Glide.with(pubChatAdapter.mContext)
                .load(data.sendUser.avatar)
                .into(helper.binding.ivAvatar)
            helper.binding.tvContent.text = pubChatAdapter.showHtml(data).toHtml() ?: ""
        }
    }
}