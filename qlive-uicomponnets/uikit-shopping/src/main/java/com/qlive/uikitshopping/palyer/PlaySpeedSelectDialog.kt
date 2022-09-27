package com.qlive.uikitshopping.palyer

import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitshopping.databinding.KitItemPlaySpeedBinding
import com.qlive.uikitshopping.databinding.KitShoppingPlayerMultipleBinding

class PlaySpeedSelectDialog : ViewBindingDialogFragment<KitShoppingPlayerMultipleBinding>() {

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

    override fun init() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.tvClose.setOnClickListener {
            dismiss()
        }
        binding.rcyMultiple.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcyMultiple.adapter = PlaySpeedAdapter()
        (binding.rcyMultiple.adapter as PlaySpeedAdapter).setNewData(
            ArrayList<PlaySpeed>(
                mPlaySpeeds
            )
        )
    }

    class PlaySpeed(
        val speedName: String,
        val speedValue: Float,
        var isSelected: Boolean = false
    )

    inner class PlaySpeedAdapter : QSmartViewBindAdapter<PlaySpeed, KitItemPlaySpeedBinding>() {

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemPlaySpeedBinding>,
            item: PlaySpeed
        ) {
            if (data.indexOf(item) == data.size - 1) {
                helper.binding.lineView.visibility = View.GONE
            } else {
                helper.binding.lineView.visibility = View.VISIBLE
            }
            helper.binding.tvSpeedName.text = item.speedName
            helper.binding.rbSelected.isChecked = item.isSelected
            helper.binding.rbSelected.isClickable = !item.isSelected
            helper.itemView.setOnClickListener {
                helper.binding.rbSelected.performClick()
            }
            helper.binding.rbSelected.setOnCheckedChangeListener { compoundButton, b ->
                data.forEach { it.isSelected = false }
                item.isSelected = b
                mDefaultListener?.onDialogPositiveClick(this@PlaySpeedSelectDialog, item)
                dismiss()
            }
        }
    }
}