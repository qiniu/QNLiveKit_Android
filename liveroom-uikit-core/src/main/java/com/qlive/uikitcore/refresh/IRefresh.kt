package com.qlive.uikitcore.refresh

import android.view.View
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

interface IRefresh {
    fun clear()
    fun getAttachView(): View
    var topOffset: Int
    var isRefresh: Boolean
    fun onPointDown()
    fun onPointMove(offset: Float)
    fun onPointUp(overscrollTop: Float)
    fun setTargetOffsetTopAndBottom(i: Int, b: Boolean)
    fun setRefreshListener(mListener: OnRefreshListener?)
    fun setRefreshing(refresh: Boolean)
}