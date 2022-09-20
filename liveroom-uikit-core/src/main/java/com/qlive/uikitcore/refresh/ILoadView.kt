package com.qlive.uikitcore.refresh

import android.content.Context
import android.view.View
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

abstract class ILoadView(val mContext: Context, val mParent: QRefreshLayout) {

    var currentHeight: Int = 0
    var isLoading: Boolean = false
    abstract fun clear()
    abstract fun getAttachView(): View
    abstract val defaultHeight: Int
    abstract fun onPointMove(height: Int): Int
    abstract fun onPointUp(totalDistance: Float): Int
    abstract fun setRefreshListener(mListener: OnRefreshListener?)
    abstract fun checkHideNoMore()
    abstract fun finishLoadMore(isNoMore: Boolean)

}