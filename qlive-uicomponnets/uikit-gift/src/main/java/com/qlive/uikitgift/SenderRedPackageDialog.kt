package com.qlive.uikitgift

import android.view.Gravity
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitgift.databinding.KitDialogSendRedPackaBinding

class SenderRedPackageDialog : ViewBindingDialogFragment<KitDialogSendRedPackaBinding>() {
    init {
        applyDimAmount(0f)
        applyGravityStyle(Gravity.CENTER)
    }

    override fun init() {
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            val count = binding.etCount.text.toString()
            if (count.isEmpty()) {
                return@setOnClickListener
            }
            val countInt = count.toInt()
            mDefaultListener?.onDialogPositiveClick(this, countInt)
            dismiss()
        }
    }
}