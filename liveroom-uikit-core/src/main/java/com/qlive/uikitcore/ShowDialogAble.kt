package com.qlive.uikitcore

import androidx.fragment.app.DialogFragment
import com.qlive.uikitcore.dialog.FinalDialogFragment

interface ShowDialogAble {
    fun showDialog(
        code: Int,
        arg: Any,
        listener: FinalDialogFragment.BaseDialogListener?=null
    ): DialogFragment


}