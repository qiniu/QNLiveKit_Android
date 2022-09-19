package com.qlive.uikitcore.smartrecycler

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.qlive.uikitcore.refresh.QRefreshLayout
import com.qlive.uikitcore.smartrecycler.IEmptyView.*

/**
 * 上拉下拉帮助类
 *
 */
open class SmartRefreshHelper<T>(
    val context: Context,
    val adapter: IAdapter<T>,
    private val recycler_view: RecyclerView,
    private val refresh_layout: QRefreshLayout,
    private val emptyCustomView: IEmptyView?,
    private val isNeedLoadMore: Boolean = true,
    private val refreshNeed: Boolean = true,
    /**
     * 刷新回调
     */
    private val fetcherFuc: (page: Int) -> Unit
) {
    private var isLoadMoreing: Boolean = false
    private var isRefreshing: Boolean = false
    private var currentPage = 0
    private var eachPageSize: Int = 0

    init {
        refresh_layout.isNoMoreEnable = (isNeedLoadMore)
        refresh_layout.isReFreshEnable = (refreshNeed)
        refresh_layout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onStartRefresh() {
                startRefresh()
            }

            override fun onStartLoadMore() {
                loadMore()
            }
        })
    }

    private fun startRefresh() {
        if (adapter.isCanShowEmptyView()) {
            emptyCustomView?.setStatus(START_REFREASH_WHEN_EMPTY)
        }
        isRefreshing = true
        fetcherFuc(0)
    }

    /**
     * 获取到分页数据 设置下拉刷新和上拉的状态
     */
    fun onFetchDataFinish(data: MutableList<T>?) {
        onFetchDataFinish(data, true)
    }

    fun onFetchDataFinish(data: MutableList<T>?, goneIfNoData: Boolean, sureLoadMoreEnd: Boolean?) {
        if (isRefreshing) {
            refresh_layout.finishRefresh(data?.isEmpty() ?: true)
        } else {
            refresh_layout.finishLoadMore(sureLoadMoreEnd ?: false)
        }
        if (data != null) {
            if (currentPage == 0 && isRefreshing) {
                eachPageSize = data.size
            }
            if (isLoadMoreing) {
                currentPage++
                adapter.addDataList(data)
            } else {
                adapter.setNewDataList((data))
                currentPage = 0
            }
        }
        refreshEmptyView(NODATA)
        isLoadMoreing = false
        isRefreshing = false
    }

    fun onFetchDataFinish(data: MutableList<T>?, goneIfNoData: Boolean) {
        onFetchDataFinish(data, goneIfNoData, null)
    }

    /**
     * 加载数据失败
     */
    fun onFetchDataError() {
        if (isRefreshing) {
            refresh_layout.finishRefresh(false)
        } else {
            refresh_layout.finishLoadMore(false)
        }
        val disconnected = !NetUtil.isNetworkAvailable(recycler_view.context)
        if (disconnected) {
            refreshEmptyView(NETWORK_ERROR)
        } else {
            refreshEmptyView(NODATA)
        }
        isLoadMoreing = false
        isRefreshing = false
    }

    private fun refreshEmptyView(type: Int) {
        if (adapter.isCanShowEmptyView()) {
            //if (adapter.data.isEmpty() && adapter.headerLayoutCount + adapter.footerLayoutCount == 0) {
            emptyCustomView?.setStatus(type)
        } else {
            emptyCustomView?.setStatus(HIDE_LAYOUT)
        }
    }

    private fun loadMore() {
        if (isRefreshing || isLoadMoreing) {
            if (isLoadMoreing) {
                isLoadMoreing = false
            }
            return
        }
        isLoadMoreing = true
        fetcherFuc(currentPage + 1)
    }

    fun refresh() {
        if (isRefreshing || isLoadMoreing) {
            if (isRefreshing) {
                isRefreshing = false
            }
            refresh_layout.startRefresh()
            startRefresh()
            return
        }
        refresh_layout.startRefresh()
        startRefresh()
    }
}