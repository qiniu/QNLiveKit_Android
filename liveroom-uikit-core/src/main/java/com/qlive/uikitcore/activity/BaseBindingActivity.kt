package com.qlive.uikitcore.activity

import androidx.viewbinding.ViewBinding
import com.qlive.uikitcore.ext.ViewBindingExt
import java.lang.reflect.ParameterizedType

abstract class BaseBindingActivity<T : ViewBinding> : BaseFrameActivity() {

    lateinit var binding: T

    override fun setContentView() {
        val sup = javaClass.genericSuperclass
        binding = ViewBindingExt.create(sup as ParameterizedType, null, this, false)
        setContentView(binding.root)
    }

    final override fun getLayoutId(): Int {
        return -1
    }
}