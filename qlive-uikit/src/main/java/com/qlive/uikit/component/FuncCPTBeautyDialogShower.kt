package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import com.qlive.uikitcore.BeautyHook
import com.qlive.uikitcore.QLiveFuncComponent

class FuncCPTBeautyDialogShower(context: Context) : QLiveFuncComponent(context, null) {

    fun showBeautyEffectDialog() {
        BeautyHook.showBeautyEffectDialog.invoke(kitContext!!.fragmentManager)
    }

    fun showBeautyStickDialog() {
        BeautyHook.showBeautyStickDialog.invoke(kitContext!!.fragmentManager)
    }
}