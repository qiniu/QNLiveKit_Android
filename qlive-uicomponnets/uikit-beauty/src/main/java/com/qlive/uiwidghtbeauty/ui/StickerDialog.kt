package com.qlive.uiwidghtbeauty.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.qlive.uiwidghtbeauty.R

class StickerDialog: BeautyDialog() {
    private var mStickerView : StickerView?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mStickerView == null) {
            mStickerView =
                StickerView(requireContext())
        }
        if (mStickerView?.parent != null) {
            (mStickerView?.parent as ViewGroup).removeView(mStickerView)
        }
        val view = inflater.inflate(R.layout.kit_beauty_effect_dialog, container, false)
        view.findViewById<FrameLayout>(R.id.flcontainer).addView(mStickerView)
        return view
    }
}