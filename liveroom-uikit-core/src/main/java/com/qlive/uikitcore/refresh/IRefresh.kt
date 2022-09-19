package com.qlive.uikitcore.refresh

import android.view.View
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

interface IRefresh {
    /**
     * 重置refreshView 状态
     */
    fun reset()

    /**
     * 创建refreshView
     *
     * @return refreshView
     */
    fun getAttachView(): View

    /**
     * @return refreshview 的z轴顺序
     */
    var zIndex: ZOder

    /**
     * @return refreshView 现在距顶部的距离
     */
    var currentTargetOffsetTop: Int

    /**
     * 是否正在刷新
     *
     * @return true:正在刷新 false:
     */
    var isRefresh: Boolean

    /**
     * 开始下拉
     */
    fun startPulling()

    /**
     * 下拉中，refreshview显示动画在此实现
     *
     * @param overscrollTop 下拉总距离
     */
    fun showPullRefresh(overscrollTop: Float)

    /**
     * 下拉结束，根据下拉距离，判断是否应该刷新
     *
     * @param overscrollTop 下拉总距离
     */
    fun finishPullRefresh(overscrollTop: Float)

    /**
     * 设置refreview 的top值
     *
     * @param i refreshview 距离顶部的距离
     * @param b 是否提示父view 重绘
     */
    fun setTargetOffsetTopAndBottom(i: Int, b: Boolean)

    /**
     * 设置refresh 监听回调
     *
     * @param mListener
     */
    fun setRefreshListener(mListener: OnRefreshListener?)

    /**
     * 设置 refresh 状态
     *
     * @param refresh
     */
    fun setRefreshing(refresh: Boolean)
}