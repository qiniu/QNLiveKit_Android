package com.qlive.uikitcore.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.*
import kotlin.math.abs


class QRefreshLayout : FrameLayout, NestedScrollingParent, NestedScrollingChild {

    private val TAG = "QSwipeRefreshLayout"
    private val mNestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }
    private val mNestedScrollingParentHelper by lazy { NestedScrollingParentHelper(this) }
    private val configuration by lazy { ViewConfiguration.get(context) }

    //fling最小速度
    private val mMinimumVelocity: Int by lazy { configuration.scaledMinimumFlingVelocity }
    private val mScroller: OverScroller by lazy { OverScroller(getContext()) }
    private var mListener: OnRefreshListener? = null
    private lateinit var mRefreshView: View
    private lateinit var mFooterView: View
    private lateinit var mScrollView: View
    private lateinit var mRefreshController: IRefresh
    private lateinit var mLoadViewController: ILoadView //上拉距离
    private var mUpTotalUnconsumed = 0f//下拉距离
    private var mDownTotalUnconsumed = 0f
    private var mNestedScrollInProgress = false

    var isNoMoreEnable = true
    var isReFreshEnable = true

    //扩展覆盖
    private var extCover: View? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        mScrollView = getChildAt(2)
        //只支持放第三个view 为扩展ui
        if (childCount >= 3) {
            extCover = getChildAt(3)
        }
        mRefreshView.bringToFront()
    }

    /**
     * 设置滑动监听
     *
     * @param onRefreshListener 刷新回调
     */
    fun setOnRefreshListener(onRefreshListener: OnRefreshListener?) {
        mListener = onRefreshListener
        mLoadViewController.setRefreshListener(mListener)
        mRefreshController.setRefreshListener(mListener)
    }

    fun startRefresh() {
        mRefreshController.setRefreshing(true)
    }

    fun finishRefresh(isEmpty: Boolean) {
        mRefreshController.setRefreshing(false)
        if (!isEmpty) {
            mLoadViewController.checkHideNoMore()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun finishLoadMore(noMore: Boolean) {
        mLoadViewController.finishLoadMore(noMore)
    }

    override fun setEnabled(enable: Boolean) {
        super.setEnabled(enable)
        if (!enable) {
            reset()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    override fun onLayout(b: Boolean, i: Int, i1: Int, i2: Int, i3: Int) {
        Log.d(TAG, "onLayout: $childCount")
        val width = measuredWidth
        val height = measuredHeight
        if (childCount == 0) {
            return
        }
        val child = mScrollView
        val childLeft = paddingLeft
        val childRight = paddingRight
        val childTop = paddingTop
        val childBottom = paddingBottom
        val childWidth = width - childLeft - childRight
        val childHeight = height - childTop - childBottom
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        val circleWidth = mRefreshView.measuredWidth
        val circleHeight = mRefreshView.measuredHeight
        val loadViewWidth = mFooterView.measuredWidth
        val loadViewHeight = mFooterView.measuredHeight
        mRefreshView.layout(
            width / 2 - circleWidth / 2, mRefreshController.topOffset,
            width / 2 + circleWidth / 2, mRefreshController.topOffset + circleHeight
        )
        val layoutParams = mFooterView.layoutParams
        if (layoutParams is MarginLayoutParams) {
            val lp = mFooterView.layoutParams as MarginLayoutParams
            mFooterView.layout(
                width / 2 - loadViewWidth / 2,
                height - childBottom + lp.topMargin,
                width / 2 + loadViewWidth / 2,
                height + loadViewHeight + childBottom + lp.bottomMargin
            )
        } else {
            mFooterView.layout(
                width / 2 - loadViewWidth / 2, height - childBottom, width / 2 + loadViewWidth / 2,
                height + loadViewHeight + childBottom
            )
        }
        extCover?.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (!canChildScrollDown() && canLoadMore()) {
                val dis =
                    mLoadViewController.onPointUp((mScroller.finalY - mScroller.currY).toFloat())
                scrollBy(0, dis)
                mScroller.abortAnimation()
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun reset() {
        mRefreshController.clear()
        val height = mLoadViewController.currentHeight
        if (height > 0) {
            clearAnimation()
            scrollBy(0, -height)
        }
        mLoadViewController.clear()
    }

    private fun flingWithNestedDispatch(velocityX: Float, velocityY: Float): Boolean {
        val canFling = abs(velocityY) > mMinimumVelocity
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            dispatchNestedFling(velocityX, velocityY, canFling)
            if (canFling) {
                return fling(velocityY)
            }
        }
        return false
    }

    private fun fling(velocityY: Float): Boolean {
        if (velocityY <= 0) {
            if (mLoadViewController.currentHeight > 0) {
                moveLoadMoreViewDown(mLoadViewController.currentHeight)
            }
            mScroller.abortAnimation()
            return false
        }
        mScroller.abortAnimation()
        mScroller.computeScrollOffset()
        if (canChildScrollUp() && canLoadMore()) {
            mScroller.fling(
                0,
                mScroller.currY,
                0,
                velocityY.toInt(),
                0,
                0,
                Int.MIN_VALUE,
                Int.MAX_VALUE
            )
        }
        ViewCompat.postInvalidateOnAnimation(this)
        return false
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return ((isNoMoreEnable || isReFreshEnable) && !mRefreshController.isRefresh
                && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes)
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mUpTotalUnconsumed = 0f
        mDownTotalUnconsumed = 0f
        mNestedScrollInProgress = true
    }

    override fun onStopNestedScroll(target: View) {
        mNestedScrollInProgress = false
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        if (mUpTotalUnconsumed > 0) {
            mRefreshController.onPointUp(mUpTotalUnconsumed)
            mUpTotalUnconsumed = 0f
        }
        if (mDownTotalUnconsumed > 0) {
            val dis = mLoadViewController.onPointUp(mDownTotalUnconsumed)
            scrollBy(0, dis)
            mDownTotalUnconsumed = 0f
        }
        stopNestedScroll()
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        // 子view 处理完后让父亲消费

        //dy 《 0 下拉 dy 》0 上拉
        val mParentOffsetInWindow = IntArray(2)

        dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            mParentOffsetInWindow
        )

        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (mRefreshController.isRefresh) {
            return
        }
        //下拉 并且刚到顶端
        if (dy < 0 && !canChildScrollUp() && canRefresh()) {
            //下拉距离
            mUpTotalUnconsumed += Math.abs(dy).toFloat()
            //moveSpinner(mUpTotalUnconsumed);
            mRefreshController.onPointMove(mUpTotalUnconsumed)
        } else if (dy > 0 && !canChildScrollDown() && canLoadMore()) {
            //上拉 刚到低端
            mDownTotalUnconsumed += dy.toFloat()
            moveMoreViewUp(dy)
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        if (isNoMoreEnable || isReFreshEnable) {
            if (dy > 0 && mUpTotalUnconsumed > 0) {
                if (dy > mUpTotalUnconsumed) {
                    consumed[1] = dy - mUpTotalUnconsumed.toInt()
                    mUpTotalUnconsumed = 0f
                } else {
                    mUpTotalUnconsumed -= dy.toFloat()
                    consumed[1] = dy
                }
                mRefreshController.onPointMove(mUpTotalUnconsumed)
            } else if (dy < -1 && mLoadViewController.currentHeight > 0) {
                if (dy + mDownTotalUnconsumed < 0) {
                    consumed[1] = dy + mDownTotalUnconsumed.toInt()
                    mDownTotalUnconsumed = 0f
                } else {
                    mDownTotalUnconsumed += dy.toFloat()
                    consumed[1] = dy
                }
                moveLoadMoreViewDown(Math.abs(dy))
            }
        }
        val parentConsumed = IntArray(2)
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        if (!consumed) {
            flingWithNestedDispatch(velocityX, velocityY)
            return true
        }
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return flingWithNestedDispatch(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun canChildScrollUp(): Boolean {
        return if (Build.VERSION.SDK_INT < 14) {
            if (mScrollView is AbsListView) {
                val absListView = mScrollView as AbsListView
                (absListView.childCount > 0
                        && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(0)
                    .top < absListView.paddingTop))
            } else {
                mScrollView.canScrollVertically(-1) || mScrollView.scrollY > 0
            }
        } else {
            mScrollView.canScrollVertically(-1)
        }
    }

    /**
     * target view 是否能向下滑动
     *
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun canChildScrollDown(): Boolean {
        return if (Build.VERSION.SDK_INT < 14) {
            if (mScrollView is AbsListView) {
                val absListView = mScrollView as AbsListView
                val count = absListView.childCount
                val position = absListView.lastVisiblePosition
                count > position + 1 || absListView.getChildAt(position).bottom <= absListView.paddingBottom
            } else {
                mScrollView.canScrollVertically(1)
            }
        } else {
            mScrollView.canScrollVertically(1)
        }
    }

    private fun moveMoreViewUp(height: Int) {
        scrollBy(0, mLoadViewController.onPointMove(height))
    }

    private fun moveLoadMoreViewDown(h: Int) {
        var height = h
        if (mLoadViewController.currentHeight > 0) {
            val currentHeight = mLoadViewController.currentHeight
            if (height > currentHeight) {
                height = currentHeight
            }
            scrollBy(0, mLoadViewController.onPointMove(-height))
        } else {
            mLoadViewController.clear()
        }
    }

    private fun canRefresh(): Boolean {
        return isReFreshEnable
    }

    private fun canLoadMore(): Boolean {
        return isNoMoreEnable && canChildScrollUp()
    }

    interface OnRefreshListener {
        fun onStartRefresh()
        fun onStartLoadMore()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mLoadViewController =
            DefaultLoadView(context, this)
        mRefreshController =
            DefaultRefreshView(context, this)
        mRefreshView = mRefreshController.getAttachView()
        mFooterView = mLoadViewController.getAttachView()
        addView(mFooterView, mFooterView.layoutParams)
        addView(mRefreshView)
        isChildrenDrawingOrderEnabled = true
    }
}