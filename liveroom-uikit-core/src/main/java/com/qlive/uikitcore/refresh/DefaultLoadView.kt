package com.qlive.uikitcore.refresh


import android.content.Context
import android.util.DisplayMetrics
import android.view.animation.Animation
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import androidx.annotation.ColorInt
import com.qlive.uikitcore.R
import java.lang.Exception

open class DefaultLoadView(private val mContext: Context, private val mParent: QRefreshLayout) :
    ILoadView {
    private val metrics: DisplayMetrics
    private val mDecelerateInterpolator: DecelerateInterpolator
    private val mCircleImageView: CircleImageView
    private val mProgress: ProgressDrawable
    private var mDefaultProgressColor = 0
    override var currentHeight = 0
    override var isLoading = false

    //loadview 大小
    private val mCircleDiameter: Int
    private var mMargin = 5
    override var defaultHeight: Int = 0

    //加载更多动画
    private val mAnimationShowLoadMore: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val offset = ((defaultHeight - currentHeight) * interpolatedTime).toInt()
            mParent.scrollBy(0, move(offset))
        }
    }
    private var mListener: OnRefreshListener? = null

    //动画是否在加载
    @Volatile
    private var isLoadAnimation = false

    //是否显示没有更多view
    private var mShowNoMore = false

    //loadmore动画结束，调用回调函数
    private val mLoadMoreListener: Animation.AnimationListener =
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isLoadAnimation = true
            }

            override fun onAnimationEnd(animation: Animation) {
                if (!mShowNoMore) beginLoading()
                isLoadAnimation = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

    override fun getAttachView(): View {
        return mCircleImageView
    }

    override fun setRefreshListener(mListener: OnRefreshListener?) {
        this.mListener = mListener
    }

    override fun setLoadMore(loading: Boolean) {
        if (isLoading != loading) {
            isLoading = loading
            if (loading) {
                animateShowLoadMore(mLoadMoreListener)
            } else {
                animateHideLoadMore()
            }
        }
    }

    override fun stopAnimation() {
        mProgress.stop()
    }

    /**
     * 实际移动距离
     *
     * @param height
     * @return
     */
    override fun move(height: Int): Int {
        currentHeight += height
        if (currentHeight > defaultHeight) {
            val result = height - (currentHeight - defaultHeight)
            currentHeight = defaultHeight
            return result
        } else if (currentHeight < 0) {
            val result = height - currentHeight
            currentHeight = 0
            return result
        }
        return height
    }

    protected fun beginLoading() {
        mProgress.alpha = MAX_ALPHA
        mProgress.start()
        if (!isLoading && mListener != null) {
            isLoading = true
            mListener!!.onStartLoadMore()
        }
    }

    override fun reset() {
        isLoading = false
        if (mProgress.isRunning) mProgress.stop()
        currentHeight = 0
    }

    override fun finishPullRefresh(totalDistance: Float): Int {
        if (isLoadAnimation) return 0
        //beginLoading();
        animateShowLoadMore(mLoadMoreListener)
        return 0
    }

    fun setProgressColors(@ColorInt vararg colors: Int) {
        mProgress.setColorSchemeColors(*colors)
    }

    //显示加载更多view
    private fun animateShowLoadMore(listener: Animation.AnimationListener) {
        mAnimationShowLoadMore.reset()
        mAnimationShowLoadMore.duration = ANIMATE_TO_TRIGGER_DURATION.toLong()
        mAnimationShowLoadMore.interpolator = mDecelerateInterpolator
        mAnimationShowLoadMore.setAnimationListener(listener)

        mParent.clearAnimation()
        mParent.startAnimation(mAnimationShowLoadMore)
    }

    private fun animateHideLoadMore() {
        mParent.clearAnimation()
        mParent.scrollBy(0, -currentHeight)
        reset()
    }

    override fun showNoMore(show: Boolean) {
        mShowNoMore = show
        if (show) {
            isLoading = false
        }
        if (mProgress.isRunning) mProgress.stop()
    }

    companion object {
        //默认circleimage大小
        const val CIRCLE_DIAMETER = 40

        // Max amount of circle that can be filled by progress during swipe gesture,
        // where 1.0 is a full circle
        private const val MAX_PROGRESS_ANGLE = .8f
        private const val MAX_ALPHA = 255

        // Default background for the progress spinner
        private const val CIRCLE_BG_LIGHT = -0x50506
        private const val ANIMATE_TO_TRIGGER_DURATION = 200
        private const val DECELERATE_INTERPOLATION_FACTOR = 2f
    }

    init {

        metrics = mContext.resources.displayMetrics
        mCircleDiameter = (CIRCLE_DIAMETER * metrics.density).toInt()
        mMargin = (mMargin * metrics.density).toInt()
        defaultHeight = mMargin * 2 + mCircleDiameter
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)

        mCircleImageView = CircleImageView(mContext, CIRCLE_BG_LIGHT)
        mProgress = ProgressDrawable(mContext, mParent)
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT)
        mProgress.setRotation(MAX_PROGRESS_ANGLE)

        mCircleImageView.setImageDrawable(mProgress)
        val marginLayoutParams = MarginLayoutParams(mCircleDiameter, mCircleDiameter)
        marginLayoutParams.setMargins(0, mMargin, 0, mMargin)
        mCircleImageView.layoutParams = marginLayoutParams
    }
}