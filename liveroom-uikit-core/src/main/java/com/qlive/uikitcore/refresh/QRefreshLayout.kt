package com.qlive.uikitcore.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.OverScroller
import androidx.core.view.*
import androidx.customview.widget.ViewDragHelper

enum class ZOder(val z: Int) {
    NORMAL(0),
    TOP(1),
    BOTTOM(2)
}

class QRefreshLayout : ViewGroup, NestedScrollingParent, NestedScrollingChild {

    private val TAG = "QSwipeRefreshLayout"
    private val mNestedScrollingChildHelper: NestedScrollingChildHelper
    private val mNestedScrollingParentHelper: NestedScrollingParentHelper
    private val mParentScrollConsumed = IntArray(2)
    private val mParentOffsetInWindow = IntArray(2)
    var circleViewIndex = ZOder.NORMAL.z
    private var mListener: OnRefreshListener? = null
    private var mRefreshView: View
    private var mFooterView: View
    private lateinit var mScrollView: View

    //上拉距离
    private var mUpTotalUnconsumed = 0f

    //下拉距离
    private var mDownTotalUnconsumed = 0f

    private var mInitialMotionY = 0f
    private var mInitialDownY = 0f
    private var mIsBeingDragUp = false
    private var mIsBeingDragDown = false
    private var mActivePointerId = ViewDragHelper.INVALID_POINTER
    private val mTouchSlop: Int
    private var mNestedScrollInProgress = false

    // Target is returning to its start offset because it was cancelled or a
    // refresh was triggered.
    private var mReturningToStart = false
    private val mScroller: OverScroller
    private var mVelocityTracker: VelocityTracker? = null
    private val mMaximumVelocity: Int

    //fling最小速度
    private val mMinimumVelocity: Int
    private var mRefreshController: IRefresh
    private var mLoadViewController: ILoadView
    private var mNoMoreView: View? = null

    //onInterceptTouchEvent或onTouch move时上一点
    private var mLastY = 0f
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
        if (!isEmpty && isShowNoMore) {
            showNoMore(false)
        }
    }

    fun finishLoadMore(noMore: Boolean) {
        showNoMore(noMore)

        val height = mLoadViewController.currentHeight
        val oldStatus = mLoadViewController.isLoading
        mLoadViewController.setLoadMore(false)
        if (oldStatus) {
            mLoadViewController.stopAnimation()
            mLoadViewController.setLoadMore(false)
            if (mScrollView is AbsListView) {
                if (Build.VERSION.SDK_INT > 18) {
                    (mScrollView as AbsListView).scrollListBy(height)
                } else {
                    mScrollView.scrollBy(0, height)
                }
            } else {
                mScrollView.scrollBy(0, height)
            }
        }
    }

    /**
     * 设置没有更多提示view
     *
     * @param view         数据加载完毕没有更多数据时显示
     * @param layoutParams view 的LayoutParams
     */
    fun setNoMoreView(view: View?, layoutParams: LayoutParams?) {
        mNoMoreView = view
        mNoMoreView!!.layoutParams = layoutParams
        // mLoadMoreView.setVisibility(GONE);
    }

    /**
     * 显示没有更多提示
     *
     * @param show true:滑动到底部时显示没有更多，false:滑动到底部时显示加载更多
     */
    private var isShowNoMore = false
    private fun showNoMore(show: Boolean) {
        isShowNoMore = show
        mLoadViewController.showNoMore(show)
        if (show && mNoMoreView != null && mFooterView !== mNoMoreView) {
            // 开启 nomore  并且 没有重复mNoMoreView
            mFooterView.clearAnimation()
            detachViewFromParent(mFooterView)
            removeDetachedView(mFooterView, false)
            mFooterView = mNoMoreView!!
            addView(mNoMoreView, 0, mNoMoreView!!.layoutParams)
        } else if (!show && mFooterView !== mLoadViewController.getAttachView()) {
            //关闭 nomore  并且 不在 load
            scrollBy(0, -mLoadViewController.currentHeight)
            mLoadViewController.reset()
            detachViewFromParent(mFooterView)
            removeDetachedView(mFooterView, false)
            mFooterView = mLoadViewController.getAttachView()
            addView(mFooterView, 0)
        }
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
            width / 2 - circleWidth / 2, mRefreshController.currentTargetOffsetTop,
            width / 2 + circleWidth / 2, mRefreshController.currentTargetOffsetTop + circleHeight
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
                    mLoadViewController.finishPullRefresh((mScroller.finalY - mScroller.currY).toFloat())
                scrollBy(0, dis)
                mScroller.abortAnimation()
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return if (circleViewIndex < 0 || mRefreshController.zIndex == ZOder.NORMAL) {
            i
        } else if (mRefreshController.zIndex == ZOder.TOP) {
            if (i == circleViewIndex) {
                childCount - 1
            } else if (i > circleViewIndex) {
                i - 1
            } else {
                i
            }
        } else {
            if (i > circleViewIndex) {
                i + 1
            } else if (i == circleViewIndex) {
                0
            } else {
                i
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mScrollView.measure(
            MeasureSpec.makeMeasureSpec(
                measuredWidth - paddingLeft - paddingRight,
                MeasureSpec.EXACTLY
            ), MeasureSpec.makeMeasureSpec(
                measuredHeight - paddingTop - paddingBottom, MeasureSpec.EXACTLY
            )
        )
        extCover?.let {
            measureChild(it)
        }
        measureChild(mRefreshView)
        measureChild(mFooterView)
        circleViewIndex = -1
        // Get the index of the circleview.
        for (index in 0 until childCount) {
            if (getChildAt(index) === mRefreshView) {
                circleViewIndex = index
                break
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val pointerIndex: Int
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false
        }
        if (!isEnabled || mReturningToStart
            || mRefreshController.isRefresh || mNestedScrollInProgress
        ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mIsBeingDragUp = false
                mIsBeingDragDown = false
                mActivePointerId = event.getPointerId(0)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                mInitialDownY = event.getY(pointerIndex)
                mLastY = mInitialDownY
                return false
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerIndex = MotionEventCompat.getActionIndex(event)
                if (pointerIndex < 0) {
                    return false
                }
                mActivePointerId = event.getPointerId(pointerIndex)
            }
            MotionEvent.ACTION_CANCEL -> return false
            MotionEvent.ACTION_MOVE -> {
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = event.getY(pointerIndex)
                startDragging(y)
                if (mIsBeingDragUp) {
                    val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                    if (overscrollTop > 0) {
                        mRefreshController.showPullRefresh(overscrollTop)
                    }
                } else if (mIsBeingDragDown) {
                    val dy = (y - mLastY).toInt()
                    Log.i(TAG, "lasty:$mLastY")
                    Log.i(TAG, "dy:$dy")
                    //消除抖动
                    if (dy >= 0.5) {
                        hideLoadMoreView(Math.abs(dy))
                    } else if (dy < -0.5) {
                        showLoadMoreView(Math.abs(dy))
                    }
                }
                mLastY = y
            }
            MotionEvent.ACTION_UP -> {
                pointerIndex = event.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    //  Log.e(LOG_TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    return false
                }
                if (mIsBeingDragUp) {
                    val y = event.getY(pointerIndex)
                    val overscrollTop = (y - mInitialMotionY) * DRAG_RATE
                    mIsBeingDragUp = false
                    mRefreshController.finishPullRefresh(overscrollTop)
                }
                if (mIsBeingDragDown) {
                    val y = event.getY(pointerIndex)
                    val overscrollBottom = y - mInitialMotionY
                    mIsBeingDragDown = false
                    if (overscrollBottom < 0) {
                        val dis = mLoadViewController.finishPullRefresh(Math.abs(overscrollBottom))
                        scrollBy(0, dis)
                    }
                }
                mActivePointerId = ViewDragHelper.INVALID_POINTER
                return false
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(event)
        }
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        val pointerIndex: Int
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false
        }
        if (!isEnabled || !canLoadMore() && !canRefresh() || mReturningToStart
            || mRefreshController.isRefresh || mNestedScrollInProgress
        ) {
            // Fail fast if we're not in a state where a swipe is possible
            return false
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mRefreshController.setTargetOffsetTopAndBottom(
                    mRefreshController.currentTargetOffsetTop - mRefreshView.top,
                    true
                )
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragUp = false
                mIsBeingDragDown = false
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                mInitialDownY = ev.getY(pointerIndex)
                initVelocityTracker()
                mVelocityTracker!!.addMovement(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == ViewDragHelper.INVALID_POINTER) {
                    return false
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker!!.addMovement(ev)
                }
                pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = ev.getY(pointerIndex)
                startDragging(y)
            }
            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            MotionEvent.ACTION_UP -> if (mVelocityTracker != null) {
                val velocityTracker: VelocityTracker = mVelocityTracker!!
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = velocityTracker.getYVelocity(mActivePointerId)
                Log.d(TAG, "fling:$initialVelocity")
                if (Math.abs(initialVelocity) > mMinimumVelocity) {
                    flingWithNestedDispatch(0f, -initialVelocity)
                }
                releaseVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragUp = false
                mActivePointerId = ViewDragHelper.INVALID_POINTER
            }
        }
        return mIsBeingDragUp || mIsBeingDragDown
    }

    override fun requestDisallowInterceptTouchEvent(b: Boolean) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if (Build.VERSION.SDK_INT < 21 && mScrollView is AbsListView
            || !ViewCompat.isNestedScrollingEnabled(mScrollView)
        ) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b)
        }
    }

    private fun reset() {
        mRefreshController.reset()
        val height = mLoadViewController.currentHeight
        if (height > 0) {
            clearAnimation()
            scrollBy(0, -height)
        }
        mLoadViewController.reset()
    }


    private fun initVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        } else {
            mVelocityTracker!!.clear()
        }
    }

    private fun releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    private fun measureChild(view: View?) {
        if (view == null) {
            return
        }
        val lp = view.layoutParams
        val width: Int
        val height: Int
        if (lp != null) {
            width = getMeasureSpec(lp.width, measuredWidth)
            height = getMeasureSpec(lp.height, measuredHeight)
        } else {
            width = getMeasureSpec(LayoutParams.MATCH_PARENT, measuredWidth)
            height = getMeasureSpec(LayoutParams.WRAP_CONTENT, measuredHeight)
        }
        view.measure(width, height)
    }

    private fun getMeasureSpec(size: Int, parentSize: Int): Int {
        val result: Int
        result = if (size == LayoutParams.MATCH_PARENT) {
            MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.EXACTLY)
        } else if (size == LayoutParams.WRAP_CONTENT) {
            MeasureSpec.makeMeasureSpec(parentSize, MeasureSpec.AT_MOST)
        } else {
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        }
        return result
    }

    private fun startDragging(y: Float) {
        val yDiff = y - mInitialDownY
        if (yDiff > mTouchSlop && !mIsBeingDragUp) {
            if (!canChildScrollUp()) {
                mInitialMotionY = mInitialDownY + mTouchSlop
                mIsBeingDragUp = true
                mRefreshController.startPulling()
            } else if (mLoadViewController.currentHeight > 0) {
                hideLoadMoreView(yDiff.toInt())
            }
        } else if (yDiff < -mTouchSlop && !mIsBeingDragDown && !canChildScrollDown() && canLoadMore()) {
            Log.d(TAG, "$yDiff:$mTouchSlop")
            mInitialMotionY = mInitialDownY + mTouchSlop
            mLastY = mInitialDownY
            mIsBeingDragDown = true
        }
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = MotionEventCompat.getActionIndex(ev)
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
            //mVelocityTracker.addMovement(ev);
        }
    }

    private fun flingWithNestedDispatch(velocityX: Float, velocityY: Float): Boolean {
        val canFling = Math.abs(velocityY) > mMinimumVelocity
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
                hideLoadMoreView(mLoadViewController.currentHeight)
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
            mRefreshController.finishPullRefresh(mUpTotalUnconsumed)
            mUpTotalUnconsumed = 0f
        }
        if (mDownTotalUnconsumed > 0) {
            val dis = mLoadViewController.finishPullRefresh(mDownTotalUnconsumed)
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
        dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            mParentOffsetInWindow
        )
        val dy = dyUnconsumed + mParentOffsetInWindow[1]
        if (mRefreshController.isRefresh) {
            return
        }
        if (dy < 0 && !canChildScrollUp() && canRefresh()) {
            mUpTotalUnconsumed += Math.abs(dy).toFloat()
            //moveSpinner(mUpTotalUnconsumed);
            mRefreshController.showPullRefresh(mUpTotalUnconsumed)
        } else if (dy > 0 && !canChildScrollDown() && canLoadMore()) {
            mDownTotalUnconsumed += dy.toFloat()
            showLoadMoreView(dy)
        }
    }

    /**
     * parent 消耗的值
     *
     * @param target   target view
     * @param dx       x distance
     * @param dy       y方向的移动距离
     * @param consumed parent消耗的值
     */
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
                mRefreshController.showPullRefresh(mUpTotalUnconsumed)
            } else if (dy < -1 && mLoadViewController.currentHeight > 0) {
                if (dy + mDownTotalUnconsumed < 0) {
                    consumed[1] = dy + mDownTotalUnconsumed.toInt()
                    mDownTotalUnconsumed = 0f
                } else {
                    mDownTotalUnconsumed += dy.toFloat()
                    consumed[1] = dy
                }
                hideLoadMoreView(Math.abs(dy))
            }
        }
        // Now let our nested parent consume the leftovers
        val parentConsumed = mParentScrollConsumed
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
    /********************
     * parent end
     */
    /**
     * targrt view 是否能向上滑动
     *
     * @return target view 是否能向上滑动
     */
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

    private fun showLoadMoreView(height: Int) {
        if (mFooterView.visibility != VISIBLE) {
            mFooterView.visibility = VISIBLE
        }
        scrollBy(0, mLoadViewController.move(height))
    }

    private fun hideLoadMoreView(h: Int) {
        var height = h
        if (mLoadViewController.currentHeight > 0) {
            val currentHeight = mLoadViewController.currentHeight
            if (height > currentHeight) {
                height = currentHeight
            }
            scrollBy(0, mLoadViewController.move(-height))
        } else {
            mLoadViewController.reset()
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

    companion object {
        private const val DRAG_RATE = .5f
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
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mNestedScrollingChildHelper = NestedScrollingChildHelper(this)
        mNestedScrollingParentHelper = NestedScrollingParentHelper(this)
        mScroller = OverScroller(getContext())
        mRefreshView = mRefreshController.getAttachView()
        mFooterView = mLoadViewController.getAttachView()
        addView(mFooterView, mFooterView.layoutParams)
        addView(mRefreshView)
        isChildrenDrawingOrderEnabled = true
    }
}