package com.qlive.uikitcore.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.qlive.uikitcore.ext.ViewBindingExt
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

abstract class ViewBindingDialogFragment<R : ViewBinding> : FinalDialogFragment() {

    lateinit var binding: R
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sup = javaClass.genericSuperclass
        binding =
            ViewBindingExt.create(sup as ParameterizedType, container, requireContext(), false)
        return binding.root
    }

    final override fun getViewLayoutId(): Int {
        return 0;
    }
}