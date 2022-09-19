package com.qlive.uikitcore.refresh

import android.util.DisplayMetrics
import android.view.animation.Animation
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import androidx.annotation.ColorInt

class DefaultRefreshView(private val mContext: Context, private val mParent: View) : IRefresh {
    private val metrics: DisplayMetrics = mContext.resources.displayMetrics
    private val mDecelerateInterpolator: DecelerateInterpolator =
        DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)
    private var mFrom = 0
    private var mSpinnerOffsetEnd: Int = 0

    // Whether this item is scaled up rather than clipped
    private val mScale = false
    private var mStartingScale = 0f
    private var mProgressDrawable: ProgressDrawable? = null
    override var currentTargetOffsetTop: Int = 0
    private var mOriginalOffsetTop: Int = 0
    private var mTotalDragDistance = -1f
    override var isRefresh = false
    private lateinit var mCircleView: CircleImageView
    private var mNotify = false
    private val mAnimateToStartPosition: Animation = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(interpolatedTime)
        }
    }
    private val mAnimateToCorrectPosition: Animation = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val endTarget = mSpinnerOffsetEnd + mOriginalOffsetTop
            val targetTop = mFrom + ((endTarget - mFrom) * interpolatedTime).toInt()
            val offset = targetTop - mCircleView.top
            setTargetOffsetTopAndBottom(offset, false /* requires update */)
            mProgressDrawable!!.setArrowScale(1 - interpolatedTime)
        }
    }
    private var mCircleDiameter: Int = 0
    private var mListener: OnRefreshListener? = null

    private val mRefreshListener: Animation.AnimationListener =
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation?) {
                if (isRefresh) {
                    // Make sure the progress view is fully visible
                    mProgressDrawable!!.alpha = MAX_ALPHA
                    mProgressDrawable!!.start()
                    if (mNotify && mListener != null) {
                        mListener!!.onStartRefresh()
                    }
                    currentTargetOffsetTop = mCircleView.top
                } else {
                    reset()
                }
            }
        }

    private var mScaleAnimation: Animation? = null
    private var mScaleDownAnimation: Animation? = null
    private var mAlphaStartAnimation: Animation? = null
    private var mAlphaMaxAnimation: Animation? = null
    private var mScaleDownToStartAnimation: Animation? = null

    //创建refresh view
    override fun getAttachView(): View {
        return mCircleView
    }

    //refresh 结束，资源清理
    override fun reset() {
        mCircleView.clearAnimation()
        mProgressDrawable!!.stop()
        mCircleView.visibility = View.GONE
        setColorViewAlpha(MAX_ALPHA)
        // Return the circle to its start position
        if (mScale) {
            setAnimationProgress(0f)
        } else {
            setTargetOffsetTopAndBottom(
                mOriginalOffsetTop - currentTargetOffsetTop,
                true /* requires update */
            )
        }
        currentTargetOffsetTop = mCircleView.top
    }

    /**
     * 设置progress colors
     *
     * @param colors
     */
    fun setProgressColors(@ColorInt vararg colors: Int) {
        mProgressDrawable!!.setColorSchemeColors(*colors)
    }

    override fun startPulling() {
        mProgressDrawable!!.alpha = STARTING_PROGRESS_ALPHA
    }

    //下拉时，refresh动画
    override fun showPullRefresh(overscrollTop: Float) {
        mProgressDrawable!!.showArrow(true)
        val originalDragPercent = overscrollTop / mTotalDragDistance
        val dragPercent = Math.min(1f, Math.abs(originalDragPercent))
        val adjustedPercent = Math.max(dragPercent - .4, 0.0).toFloat() * 5 / 3
        val extraOS = Math.abs(overscrollTop) - mTotalDragDistance
        val slingshotDist = mSpinnerOffsetEnd.toFloat()
        val tensionSlingshotPercent =
            Math.max(0f, Math.min(extraOS, slingshotDist * 2) / slingshotDist)
        val tensionPercent = (tensionSlingshotPercent / 4 - Math.pow(
            (tensionSlingshotPercent / 4).toDouble(), 2.0
        )).toFloat() * 2f
        val extraMove = slingshotDist * tensionPercent * 2
        val targetY = mOriginalOffsetTop + (slingshotDist * dragPercent + extraMove).toInt()
        // where 1.0f is a full circle
        if (mCircleView.visibility != View.VISIBLE) {
            mCircleView.visibility = View.VISIBLE
        }
        if (!mScale) {
            ViewCompat.setScaleX(mCircleView, 1f)
            ViewCompat.setScaleY(mCircleView, 1f)
        }
        if (mScale) {
            setAnimationProgress(Math.min(1f, overscrollTop / mTotalDragDistance))
        }
        if (overscrollTop < mTotalDragDistance) {
            if (mProgressDrawable!!.alpha > STARTING_PROGRESS_ALPHA
                && !isAnimationRunning(mAlphaStartAnimation)
            ) {
                // Animate the alpha
                startProgressAlphaStartAnimation()
            }
        } else {
            if (mProgressDrawable!!.alpha < MAX_ALPHA && !isAnimationRunning(mAlphaMaxAnimation)) {
                // Animate the alpha
                startProgressAlphaMaxAnimation()
            }
        }
        val strokeStart = adjustedPercent * .8f
        mProgressDrawable!!.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart))
        mProgressDrawable!!.setArrowScale(Math.min(1f, adjustedPercent))
        val rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f
        mProgressDrawable!!.setProgressRotation(rotation)
        setTargetOffsetTopAndBottom(targetY - currentTargetOffsetTop, true /* requires update */)
    }

    override fun setTargetOffsetTopAndBottom(offset: Int, requiresUpdate: Boolean) {
        ViewCompat.offsetTopAndBottom(mCircleView, offset)
        mCircleView.top.also { currentTargetOffsetTop = it }
        if (requiresUpdate && Build.VERSION.SDK_INT < 11) {
            mParent.invalidate()
        }
    }

    /**
     * 根据用户下拉距离，判断是否应该刷新
     *
     * @param overscrollTop 总下拉距离
     */
    override fun finishPullRefresh(overscrollTop: Float) {
        if (overscrollTop > mTotalDragDistance) {
            setRefreshing(true, true /* notify */)
        } else {
            // cancel refresh
            isRefresh = false
            mProgressDrawable!!.setStartEndTrim(0f, 0f)
            var listener: Animation.AnimationListener? = null
            if (!mScale) {
                listener = object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        if (!mScale) {
                            startScaleDownAnimation(null)
                        }
                    }
                    override fun onAnimationRepeat(animation: Animation) {}
                }
            }
            animateOffsetToStartPosition(currentTargetOffsetTop, listener)
            mProgressDrawable!!.showArrow(false)
        }
    }

    override fun setRefreshListener(mListener: OnRefreshListener?) {
        this.mListener = mListener
    }

    override fun setRefreshing(refreshing: Boolean) {
        if (refreshing && isRefresh != refreshing) {
            // scale and show
            isRefresh = refreshing
            val endTarget = mSpinnerOffsetEnd + mOriginalOffsetTop
            setTargetOffsetTopAndBottom(
                endTarget - currentTargetOffsetTop,
                true /* requires update */
            )
            mNotify = false
            startScaleUpAnimation(mRefreshListener)
        } else {
            setRefreshing(refreshing, false /* notify */)
        }
    }

    override var zIndex: ZOder = ZOder.TOP

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (isRefresh != refreshing) {
            mNotify = notify
            // ensureTarget();
            isRefresh = refreshing
            if (isRefresh) {
                animateOffsetToCorrectPosition(currentTargetOffsetTop, mRefreshListener)
            } else {
                startScaleDownAnimation(mRefreshListener)
            }
        }
    }

    private fun isAnimationRunning(animation: Animation?): Boolean {
        return animation != null && animation.hasStarted() && !animation.hasEnded()
    }

    /**
     * Pre API 11, this does an alpha animation.
     *
     */
    fun setAnimationProgress(progress: Float) {
        if (isAlphaUsedForScale) {
            setColorViewAlpha((progress * MAX_ALPHA).toInt())
        } else {
            ViewCompat.setScaleX(mCircleView, progress)
            ViewCompat.setScaleY(mCircleView, progress)
        }
    }

    private fun setColorViewAlpha(targetAlpha: Int) {
        mCircleView!!.background.alpha = targetAlpha
        mProgressDrawable!!.alpha = targetAlpha
    }

    private fun startProgressAlphaStartAnimation() {
        mAlphaStartAnimation =
            startAlphaAnimation(mProgressDrawable!!.alpha, STARTING_PROGRESS_ALPHA)
    }

    private fun startProgressAlphaMaxAnimation() {
        mAlphaMaxAnimation = startAlphaAnimation(mProgressDrawable!!.alpha, MAX_ALPHA)
    }

    private fun startAlphaAnimation(startingAlpha: Int, endingAlpha: Int): Animation? {
        // Pre API 11, alpha is used in place of scale. Don't also use it to
        // show the trigger point.
        if (mScale && isAlphaUsedForScale) {
            return null
        }
        val alpha: Animation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                mProgressDrawable?.alpha =
                    (startingAlpha + (endingAlpha - startingAlpha) * interpolatedTime).toInt()
            }
        }
        alpha.duration = ALPHA_ANIMATION_DURATION.toLong()
        mCircleView.clearAnimation()
        mCircleView.setAnimationListener(null)
        mCircleView.startAnimation(alpha)
        return alpha
    }

    fun moveToStart(interpolatedTime: Float) {
        var targetTop = 0
        targetTop = mFrom + ((mOriginalOffsetTop - mFrom) * interpolatedTime).toInt()
        val offset = targetTop - mCircleView.top
        setTargetOffsetTopAndBottom(offset, false /* requires update */)
    }

    private fun animateOffsetToStartPosition(from: Int, listener: Animation.AnimationListener?) {
        if (mScale) {
            // Scale the item back down
            startScaleDownReturnToStartAnimation(from, listener)
        } else {
            mFrom = from
            mAnimateToStartPosition.reset()
            mAnimateToStartPosition.duration =
                ANIMATE_TO_START_DURATION.toLong()
            mAnimateToStartPosition.interpolator = mDecelerateInterpolator
            if (listener != null) {
                mCircleView.setAnimationListener(listener)
            }
            mCircleView.clearAnimation()
            mCircleView.startAnimation(mAnimateToStartPosition)
        }
    }

    private fun startScaleDownReturnToStartAnimation(
        from: Int,
        listener: Animation.AnimationListener?
    ) {
        mFrom = from
        mStartingScale = if (isAlphaUsedForScale) {
            mProgressDrawable!!.alpha.toFloat()
        } else {
            ViewCompat.getScaleX(mCircleView)
        }
        mScaleDownToStartAnimation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val targetScale = mStartingScale + -mStartingScale * interpolatedTime
                setAnimationProgress(targetScale)
                moveToStart(interpolatedTime)
            }
        }
        mScaleDownToStartAnimation?.setDuration(SCALE_DOWN_DURATION.toLong())
        if (listener != null) {
            mCircleView.setAnimationListener(listener)
        }
        mCircleView.clearAnimation()
        mCircleView.startAnimation(mScaleDownToStartAnimation)
    }

    fun startScaleDownAnimation(listener: Animation.AnimationListener?) {
        mScaleDownAnimation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                setAnimationProgress(1 - interpolatedTime)
            }
        }
        mScaleDownAnimation?.setDuration(SCALE_DOWN_DURATION.toLong())
        mCircleView.clearAnimation()
        mCircleView.setAnimationListener(listener)
        mCircleView.startAnimation(mScaleDownAnimation)
    }

    @SuppressLint("NewApi")
    private fun startScaleUpAnimation(listener: Animation.AnimationListener?) {
        mCircleView!!.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= 11) {
            mProgressDrawable!!.alpha = MAX_ALPHA
        }
        mScaleAnimation = object : Animation() {
            public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                setAnimationProgress(interpolatedTime)
            }
        }
        mScaleAnimation?.duration = SCALE_DOWN_DURATION.toLong()
        if (listener != null) {
            mCircleView.setAnimationListener(listener)
        }
        mCircleView.clearAnimation()
        mCircleView.startAnimation(mScaleAnimation)
    }

    private fun animateOffsetToCorrectPosition(from: Int, listener: Animation.AnimationListener?) {
        mFrom = from
        mAnimateToCorrectPosition.reset()
        mAnimateToCorrectPosition.duration = ANIMATE_TO_TRIGGER_DURATION.toLong()
        mAnimateToCorrectPosition.interpolator = mDecelerateInterpolator
        if (listener != null) {
            mCircleView.setAnimationListener(listener)
        }
        mCircleView.clearAnimation()
        mCircleView.startAnimation(mAnimateToCorrectPosition)
    }

    /**
     * Pre API 11, alpha is used to make the progress circle appear instead of scale.
     */
    private val isAlphaUsedForScale: Boolean
        private get() = Build.VERSION.SDK_INT < 11

    companion object {
        //默认circleimage大小
        const val CIRCLE_DIAMETER = 40

        // Default offset in dips from the top of the view to where the progress spinner should stop
        private const val DEFAULT_CIRCLE_TARGET = 64

        // Max amount of circle that can be filled by progress during swipe gesture,
        // where 1.0 is a full circle
        private const val MAX_PROGRESS_ANGLE = .8f
        private const val MAX_ALPHA = 255
        private const val STARTING_PROGRESS_ALPHA = (.3f * MAX_ALPHA).toInt()
        private const val ALPHA_ANIMATION_DURATION = 300
        private const val SCALE_DOWN_DURATION = 150
        private const val DECELERATE_INTERPOLATION_FACTOR = 2f
        private const val ANIMATE_TO_START_DURATION = 200
        private const val ANIMATE_TO_TRIGGER_DURATION = 200

        // Default background for the progress spinner
        private const val CIRCLE_BG_LIGHT = -0x50506
    }

    init {
        mCircleDiameter = (CIRCLE_DIAMETER * metrics.density).toInt()
        currentTargetOffsetTop = -mCircleDiameter
        mOriginalOffsetTop = currentTargetOffsetTop
        mSpinnerOffsetEnd = (DEFAULT_CIRCLE_TARGET * metrics.density).toInt()
        mTotalDragDistance = mSpinnerOffsetEnd.toFloat()

        mCircleView = CircleImageView(mContext, CIRCLE_BG_LIGHT)
        mProgressDrawable = ProgressDrawable(mContext, mParent)
        mProgressDrawable!!.setBackgroundColor(CIRCLE_BG_LIGHT)
        mCircleView.setImageDrawable(mProgressDrawable)
        mCircleView.visibility = View.GONE
        moveToStart(1.0f)
        mCircleView.layoutParams = ViewGroup.LayoutParams(mCircleDiameter, mCircleDiameter)
    }
}