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
        val w = width
        val runnable = {
            val w2 = width
            val transx = progress / 100f * w2
            binding.pkIndicator.translationX = transx
        }
        if (w == 0) {
            post(runnable)
            return
        } else {
            runnable.invoke()
        }
    }

    fun setLeftText(string: String) {
        binding.tvLeftScore.text = string
    }

    fun setRightText(string: String) {
        binding.tvRightScore.text = string
    }
}