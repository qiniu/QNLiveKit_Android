package com.qlive.uikitcore.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.qlive.uikitcore.R
import kotlinx.android.synthetic.main.kit_dialog_common_tip.*
import java.lang.reflect.Field


class CommonTipDialog : FinalDialogFragment() {

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

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_common_tip
    }

    override fun init() {
        arguments?.apply {
            val title = getString("title")
            if (TextUtils.isEmpty(title)) {
                tvTitle.visibility = View.GONE
            } else {
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = title
            }
            val content = getString("content")
            tvContent.text = (content)
            if (TextUtils.isEmpty(content)) {
                tvContent.visibility = View.GONE
            } else {
                tvContent.visibility = View.VISIBLE
            }
            val isNeedCancelBtn = getBoolean("isNeedCancelBtn", true)
            if (!isNeedCancelBtn) {
                vV.visibility = View.GONE
                btnCancel.visibility = View.GONE
            }
            val positiveText = getString("positiveText")

            if (positiveText?.isNotEmpty() == true) {
                btnConfirm.text = positiveText
            }
            val negativeText = getString("negativeText")
            if (negativeText?.isNotEmpty() == true) {
                btnCancel.text = negativeText
            }
        }
        btnCancel.setOnClickListener {
            mDefaultListener?.onDialogNegativeClick(this, Any())
            dismiss()
        }

        btnConfirm.setOnClickListener {
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
