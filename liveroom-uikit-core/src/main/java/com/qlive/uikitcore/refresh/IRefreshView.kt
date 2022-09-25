package com.qlive.uikitcore.refresh

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import com.qlive.uikitcore.refresh.QRefreshLayout.OnRefreshListener

abstract class IRefreshView(val context: Context) {

    internal var recoverAnimator: ObjectAnimator? = null
    abstract fun getFreshTopHeight():Int
    abstract fun getFreshHeight(): Int
    abstract fun getAttachView(): View
    abstract fun isFloat(): Boolean

    abstract fun onPointMove(totalY: Float, dy: Float):Float
    abstract fun onPointUp(toStartRefresh: Boolean)
    abstract fun onFinishRefresh()
}
