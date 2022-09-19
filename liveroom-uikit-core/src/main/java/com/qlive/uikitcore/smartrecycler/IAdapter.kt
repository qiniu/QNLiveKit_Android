package com.qlive.uikitcore.smartrecycler

import androidx.recyclerview.widget.RecyclerView

interface IAdapter<T> {
    fun bindRecycler(recyclerView: RecyclerView)
    fun addDataList(mutableList: MutableList<T>)
    fun setNewDataList(mutableList: MutableList<T>)
    fun isCanShowEmptyView(): Boolean
}