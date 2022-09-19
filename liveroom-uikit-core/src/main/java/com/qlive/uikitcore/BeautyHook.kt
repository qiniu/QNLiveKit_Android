package com.qlive.uikitcore

import androidx.fragment.app.FragmentManager
import com.qlive.rtclive.QInnerVideoFrameHook

object BeautyHook {
    var isEnable = QInnerVideoFrameHook.isEnable

    /**
     * 美颜弹窗
     */
     var showBeautyEffectDialog: ((fragmentM: FragmentManager) -> Unit) = {

    }

    /**
     * 贴纸弹窗
     */
     var showBeautyStickDialog: ((fragmentM: FragmentManager) -> Unit) = {

    }
}