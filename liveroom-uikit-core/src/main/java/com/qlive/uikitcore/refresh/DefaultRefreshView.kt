package com.qlive.uikitcore.refresh

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.qlive.uikitcore.R

class DefaultRefreshView(context: Context) : IRefreshView(context) {

    companion object {
        private const val CIRCLE_BG_LIGHT = -0x50506
    }

    private fun dp2px(context: Context, dpVal: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dpVal * density + 0.5f).toInt()
    }

    private var mProgressDrawable: MaterialProgressDrawable
    private var mCircleView: CircleImageView
    private var mCircleDiameter: Int = 0
    private var maxScrollHeight = 0
    private var reFreshTopHeight = 0
    private var attchView: View

    init {
        attchView = LayoutInflater.from(context).inflate(R.layout.default_refresh_view, null, false)
        maxScrollHeight = dp2px(context, 65f)
        reFreshTopHeight = dp2px(context, 10f)
        mCircleDiameter = dp2px(context, 45f)
        mCircleView = attchView.findViewById(R.id.pbProgressBar)
        mProgressDrawable = MaterialProgressDrawable(mCircleView)
        mProgressDrawable.setColorSchemeColors(-0xff6634, -0xbbbc, -0x996700, -0x559934, -0x7800)
        mProgressDrawable.alpha = 255
        mCircleView.setImageDrawable(mProgressDrawable)
    }

    override fun getFreshTopHeight(): Int {
        return reFreshTopHeight
    }

    override fun getFreshHeight(): Int {
        return mCircleDiameter
    }

    override fun maxScrollHeight(): Int {
        return mCircleDiameter
    }

    override fun getAttachView(): View {
        return attchView
    }

    override fun isFloat(): Boolean {
        return true
    }

    override fun onPointMove(totalY: Float, dy: Float) {
        val originalDragPercent = totalY / maxScrollHeight
        val dragPercent = Math.min(1f, Math.abs(originalDragPercent))
        val adjustedPercent = Math.max(dragPercent - .4, 0.0).toFloat() * 5 / 3
        val extraOS = Math.abs(totalY) - maxScrollHeight
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
        mProgressDrawable.setStartEndTrim(0f, Math.min(MAX_PROGRESS_ANGLE, strokeStart))
        mProgressDrawable.setArrowScale(Math.min(1f, adjustedPercent))
        val rotation = (-0.25f + .4f * adjustedPercent + tensionPercent * 2) * .5f
        mProgressDrawable.setProgressRotation(rotation)
    }

    override fun onPointUp(toStartRefresh: Boolean) {
        if (toStartRefresh) {
            mProgressDrawable.start()
        } else {
            mProgressDrawable.stop()
        }
    }

    override fun onFinishRefresh() {
        mProgressDrawable.stop()
    }
}