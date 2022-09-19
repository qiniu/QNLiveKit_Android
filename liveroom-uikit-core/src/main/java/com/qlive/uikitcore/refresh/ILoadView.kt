package com.qlive.uikitcore.refresh

import android.view.View
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

interface ILoadView {
    /**
     * 重置LoadView状态
     */
    fun reset()

    /**
     * 创建LoadView
     *
     * @return loadview
     */
    fun getAttachView(): View

    /**
     * loadview 默认高度
     *
     * @return 高度
     */
    var defaultHeight: Int

    /**
     * loadview 现在高度
     *
     * @return 高度
     */
    val currentHeight: Int

    /**
     * 向下滑动
     *
     * @param height 滑动距离
     * @return 父view需移动距离
     */
    fun move(height: Int): Int

    /**
     * 滑动结束
     *
     * @param totalDistance 手指离开屏幕时滑动总距离
     * @return 父view需移动距离
     */
    fun finishPullRefresh(totalDistance: Float): Int

    /**
     * 设置刷新监听回调
     *
     * @param mListener 监听回调
     */
    fun setRefreshListener(mListener: OnRefreshListener?)

    /**
     * 是否显示nomore
     *
     * @param show
     */
    fun showNoMore(show: Boolean)

    /**
     * 设置loadmore 是否显示
     *
     * @param loading false:不显示 true: 显示
     */
    fun setLoadMore(loading: Boolean)

    /**
     * @return 加载view是否正在显示
     */
    var isLoading: Boolean

    /**
     * 停止所有动画
     */
    fun stopAnimation()
}