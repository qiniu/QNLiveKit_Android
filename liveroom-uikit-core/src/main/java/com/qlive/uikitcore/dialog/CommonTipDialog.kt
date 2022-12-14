package com.qlive.uikitcore.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.qlive.uikitcore.databinding.KitDialogCommonTipBinding

class CommonTipDialog : ViewBindingDialogFragment<KitDialogCommonTipBinding>() {

    init {
        //是否可以取消 默认值 show之前可以修改
        applyCancelable(false)
        //位置 show之前可以修改
        applyGravityStyle(Gravity.CENTER)
    }

    companion object {
        //自定义提示窗口回调
        //默认使用CommonTipDialog 可以自定义返回其他的提示
        var showTipDialogCall: (tipBuild: CommonTipDialog.TipBuild, showName: String) -> FinalDialogFragment =
            { tipBuild, showName ->
                newInstance(tipBuild)
            }

        fun newInstance(
            tipBuild: CommonTipDialog.TipBuild
        ): CommonTipDialog {
            val b = Bundle()
            b.putString("title", tipBuild.tittle)
            b.putString("content", tipBuild.content)
            b.putBoolean("isNeedCancelBtn", tipBuild.isNeedCancelBtn)
            b.putString("positiveText", tipBuild.positiveText)
            b.putString("negativeText", tipBuild.negativeText)
            val f = CommonTipDialog()
            f.arguments = b
            return f
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val ft: FragmentTransaction = manager.beginTransaction()
        ft.add(this, javaClass.simpleName)
        ft.commitAllowingStateLoss()
    }

    override fun init() {
        arguments?.apply {
            val title = getString("title")
            if (TextUtils.isEmpty(title)) {
                binding.tvTitle.visibility = View.GONE
            } else {
                binding.tvTitle.visibility = View.VISIBLE
                binding.tvTitle.text = title
            }
            val content = getString("content")
            binding.tvContent.text = (content)
            if (TextUtils.isEmpty(content)) {
                binding.tvContent.visibility = View.GONE
            } else {
                binding.tvContent.visibility = View.VISIBLE
            }
            val isNeedCancelBtn = getBoolean("isNeedCancelBtn", true)
            if (!isNeedCancelBtn) {
                binding.vV.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
            }
            val positiveText = getString("positiveText")

            if (positiveText?.isNotEmpty() == true) {
                binding.btnConfirm.text = positiveText
            }
            val negativeText = getString("negativeText")
            if (negativeText?.isNotEmpty() == true) {
                binding.btnCancel.text = negativeText
            }
        }
        binding.btnCancel.setOnClickListener {
            mDefaultListener?.onDialogNegativeClick(this, Any())
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
            mDefaultListener?.onDialogPositiveClick(this, Any())
        }
    }

    public class TipBuild {
        var tittle = ""
            private set
        var content = ""
            private set
        var positiveText = ""
            private set
        var negativeText = "取消"
            private set
        var isNeedCancelBtn = true
            private set
        var dialogListener: FinalDialogFragment.BaseDialogListener? = null
            private set

        fun setTittle(tittle: String): TipBuild {
            this.tittle = tittle
            return this
        }

        fun setContent(content: String): TipBuild {
            this.content = content
            return this
        }

        fun setPositiveText(confirm: String): TipBuild {
            positiveText = confirm
            return this
        }

        fun isNeedCancelBtn(isNeedCancelBtn: Boolean): TipBuild {
            this.isNeedCancelBtn = isNeedCancelBtn
            return this
        }

        fun setNegativeText(negativeText: String): TipBuild {
            this.negativeText = negativeText
            return this
        }

        fun setListener(listener: FinalDialogFragment.BaseDialogListener): TipBuild {
            dialogListener = listener
            return this
        }

        fun build(showName: String = ""): FinalDialogFragment {
            val d = showTipDialogCall.invoke(this, showName)
            dialogListener?.apply { d.setDefaultListener(this) }
            return d
        }
    }
}
