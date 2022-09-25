package com.qlive.uikitcore.refresh

import android.content.Context
import android.view.View
import com.qlive.uikitcore.R
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

abstract class ILoadView(val context: Context) {
    var isShowLoadMore = false
        private set
    var isShowLoading = false
        private set
    var noMoreText = context.getText(R.string.no_more_tips)

    abstract fun checkHideNoMore()
    abstract fun getFreshHeight(): Int

    abstract fun getAttachView(): View

    abstract fun onPointMove(totalY: Float, dy: Float):Float
    open fun onPointUp(toStartLoad: Boolean) {
        isShowLoading = toStartLoad
    }

    open fun onFinishLoad(showNoMore: Boolean) {
        isShowLoading = false
        isShowLoadMore = showNoMore
    }
}