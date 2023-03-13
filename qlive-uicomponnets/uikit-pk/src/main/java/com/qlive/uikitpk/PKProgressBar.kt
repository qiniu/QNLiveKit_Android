package com.qlive.uikitpk

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.qlive.uikitpk.databinding.KitViewPkProgressBinding

class PKProgressBar : FrameLayout {

    private var binding: KitViewPkProgressBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = KitViewPkProgressBinding.inflate(LayoutInflater.from(context), this)
    }

    fun setProgress(progress: Int) {
        binding.pkProgressBar.progress = progress
        val with = measuredWidth
        if (with == 0) {
            return
        }
        val transx = progress / 100f * with
        binding.pkIndicator.translationX = transx
    }

    fun setLeftText(string: String){
        binding.tvLeftScore.text = string
    }

    fun setRightText(string: String){
        binding.tvRightScore.text = string
    }
}