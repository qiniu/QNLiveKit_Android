package com.qlive.uikitshopping.palyer

import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import com.qlive.uikitshopping.R
import kotlinx.android.synthetic.main.kit_item_play_speed.view.*
import kotlinx.android.synthetic.main.kit_shopping_player_multiple.*

class PlaySpeedSelectDialog : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }
    val mPlaySpeeds = listOf<PlaySpeed>(
        PlaySpeed("0.5x", 0.5f),
        PlaySpeed("0.75x", 0.75f),
        PlaySpeed("1.0x", 1f, true),
        PlaySpeed("1.25x", 1.25f),
        PlaySpeed("1.5x", 1.5f),
        PlaySpeed("2.0x", 2f),
    )

    override fun getViewLayoutId(): Int {
        return R.layout.kit_shopping_player_multiple
    }

    override fun init() {
        ivClose.setOnClickListener {
            dismiss()
        }
        tvClose.setOnClickListener {
            dismiss()
        }
        rcyMultiple.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rcyMultiple.adapter = PlaySpeedAdapter()
        (rcyMultiple.adapter as PlaySpeedAdapter).setNewData(ArrayList<PlaySpeed>(mPlaySpeeds))

    }

    class PlaySpeed(
        val speedName: String,
        val speedValue: Float,
        var isSelected: Boolean = false
    )

    inner class PlaySpeedAdapter : QSmartAdapter<PlaySpeed>(
        R.layout.kit_item_play_speed,
        ArrayList<PlaySpeed>()
    ) {

        override fun convert(holder: QRecyclerViewHolder, item: PlaySpeed) {
            if (data.indexOf(item) == data.size - 1) {
                holder.itemView.lineView.visibility = View.GONE
            } else {
                holder.itemView.lineView.visibility = View.VISIBLE
            }
            holder.itemView.tvSpeedName.text = item.speedName
            holder.itemView.rbSelected.isChecked = item.isSelected
            holder.itemView.rbSelected.isClickable = !item.isSelected
            holder.itemView.setOnClickListener {
                holder.itemView.rbSelected.performClick()
            }
            holder.itemView.rbSelected.setOnCheckedChangeListener { compoundButton, b ->
                data.forEach { it.isSelected = false }
                item.isSelected = b
                mDefaultListener?.onDialogPositiveClick(this@PlaySpeedSelectDialog, item)
                dismiss()
            }
        }
    }
}