package com.qlive.uikitcore.refresh


import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.widget.TextView
import com.qlive.uikitcore.R
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

open class DefaultLoadView(mContext: Context, mParent: QRefreshLayout) :
    ILoadView(mContext, mParent) {
    private val metrics: DisplayMetrics = mContext.resources.displayMetrics
    private val mDecelerateInterpolator: DecelerateInterpolator
    private val mCircleImageView: CircleImageView
    private val mProgress: MaterialProgressDrawable
    private val mAttachView: View
    private val tvTipView: TextView

    final override var defaultHeight: Int = 0

    //加载更多动画
    private val mAnimationShowLoadMore: Animation = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val offset = ((defaultHeight - currentHeight) * interpolatedTime).toInt()
            mParent.scrollBy(0, onPointMove(offset))
        }
    }
    private var mListener: OnRefreshListener? = null

    //动画是否在加载
    @Volatile
    private var isLoadAnimation = false

    //是否显示没有更多view
    private var isShowingLoadMore = false

    //loadmore动画结束，调用回调函数
    private val mLoadMoreListener: Animation.AnimationListener =
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isLoadAnimation = true
            }

            override fun onAnimationEnd(animation: Animation) {
                if (!isShowingLoadMore) beginLoading()
                isLoadAnimation = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

    override fun getAttachView(): View {
        return mAttachView
    }

    override fun setRefreshListener(mListener: OnRefreshListener?) {
        this.mListener = mListener
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun finishLoadMore(isNoMore: Boolean) {
        isLoading = false
        isShowingLoadMore = isNoMore
        stopAnimation()
        mParent.clearAnimation()
        if (!isNoMore) {
            mParent.scrollBy(0, -currentHeight)
            clear()
        } else {
            mCircleImageView.visibility = View.GONE
            tvTipView.text = "noMore"
        }
    }

    override fun checkHideNoMore() {
        clear()
    }

    private fun stopAnimation() {
        mProgress.stop()
    }

    override fun onPointMove(height: Int): Int {
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

    override fun clear() {
        isShowingLoadMore = false
        isLoading = false
        if (mProgress.isRunning) mProgress.stop()
        currentHeight = 0
        tvTipView.text = ""
        mCircleImageView.visibility = View.VISIBLE
    }

    override fun onPointUp(totalDistance: Float): Int {
        if (isLoadAnimation) return 0
        //beginLoading();
        animateShowLoadMore(mLoadMoreListener)
        return 0
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

    companion object {
        private const val MAX_ALPHA = 255

        // Default background for the progress spinner
        private const val CIRCLE_BG_LIGHT = -0x50506
        private const val ANIMATE_TO_TRIGGER_DURATION = 200
        private const val DECELERATE_INTERPOLATION_FACTOR = 2f
    }

    private fun dp2px(context: Context, dpVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpVal * density + 0.5f).toInt()
    }

    init {
        mAttachView =
            LayoutInflater.from(mContext).inflate(R.layout.default_loadmore_view, mParent, false)
        defaultHeight = dp2px(mContext, 40f)
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)
        mCircleImageView = mAttachView.findViewById(R.id.pbProgressBar)
        tvTipView = mAttachView.findViewById(R.id.tvTip)
        mProgress = MaterialProgressDrawable(mCircleImageView)
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT)
        mProgress.alpha = 255
        mProgress.setColorSchemeColors(-0xff6634, -0xbbbc, -0x996700, -0x559934, -0x7800)
        mCircleImageView.setImageDrawable(mProgress)
    }
}