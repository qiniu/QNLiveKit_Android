package com.qlive.uikitcore.adapter

import android.view.View
import androidx.viewbinding.ViewBinding
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

class QRecyclerViewBindHolder<T : ViewBinding>(var binding: T, view: View) :
    QRecyclerViewHolder(view)