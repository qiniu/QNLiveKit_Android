package com.qlive.uikitcore.refresh

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.qlive.uikitcore.R

class DefaultLoadView(context: Context) : ILoadView(context) {

    private val mCircleImageView: ImageView
    private val mProgress: MaterialProgressDrawable
    private val mAttachView: View
    private val tvTipView: TextView
    var defaultHeight: Int = 0

    private fun dp2px(context: Context, dpVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpVal * density + 0.5f).toInt()
    }

    companion object {
        // Default background for the progress spinner
        private const val CIRCLE_BG_LIGHT = -0x50506
    }

    init {
        mAttachView =
            LayoutInflater.from(context).inflate(R.layout.default_loadmore_view, null, false)
        defaultHeight = dp2px(context, 40f)

        mCircleImageView = mAttachView.findViewById(R.id.pbProgressBar)
        tvTipView = mAttachView.findViewById(R.id.tvTip)
        mProgress = MaterialProgressDrawable(mCircleImageView)
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT)
        mProgress.alpha = 255
        mProgress.setColorSchemeColors(-0xff6634, -0xbbbc, -0x996700, -0x559934, -0x7800)
        mCircleImageView.setImageDrawable(mProgress)
    }

    override fun checkHideNoMore() {
        onFinishLoad(false)
    }

    override fun getFreshHeight(): Int {
        return defaultHeight
    }

    override fun maxScrollHeight(): Int {
        return defaultHeight
    }

    override fun getAttachView(): View {
        return mAttachView
    }

    override fun onPointMove(totalY: Float, dy: Float) {
        if (isShowLoading || isShowLoadMore) {
            return
        }
        val originalDragPercent = totalY / defaultHeight
        val dragPercent = Math.min(1f, Math.abs(originalDragPercent))
        val adjustedPercent = Math.max(dragPercent - .4, 0.0).toFloat() * 5 / 3
        val extraOS = Math.abs(totalY) - defaultHeight
        val slingshotDist = 1f
        val tensionSlingshotPercent =
            Math.max(0f, Math.min(extraOS, slingshotDist * 2) / slingshotDist)
        val tensionPercent = (tensionSlingshotPercent / 4 - Math.pow(
            (tensionSlingshotPercent / 4).toDouble(), 2.0
        )).toFloat() * 2f
        val extraMove = slingshotDist * tensionPercent * 2
        val targetY = totalY + (slingshotDist * dragPercent + extraMove).toInt()
        val strokeStart = adjustedPercent * .8f
        val MAX_PROGRESS_ANGLE = .8f
        mProgress.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart))
        mProgress.setArrowScale(Math.min(1f, adjustedPercent))
        val rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f
        mProgress.setProgressRotation(rotation)
    }

    override fun onPointUp(toStartLoad: Boolean) {
        super.onPointUp(toStartLoad)
        if (toStartLoad) {
            mProgress.start()
        } else {
            mProgress.stop()
        }
    }

    override fun onFinishLoad(showNoMore: Boolean) {
        super.onFinishLoad(showNoMore)
        mProgress.stop()
        if (showNoMore) {
            mCircleImageView.visibility = View.GONE
            tvTipView.visibility = View.VISIBLE
            tvTipView.text = noMoreText
        } else {
            mCircleImageView.visibility = View.VISIBLE
            tvTipView.visibility = View.GONE
            tvTipView.text = ""
        }
    }

}