package com.qlive.uikitpk

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.qlive.pkservice.QPKService
import com.qlive.pkservice.QPKServiceListener
import com.qlive.pkservice.QPKSession

import com.qlive.uikitcore.QKitFrameLayout

/**
 * PK覆盖层 暂无UI
 */
class PKCoverView : QKitFrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mQPKServiceListener = object :
        QPKServiceListener {

        /**
         * pk开始 显示
         */
        override fun onStart(pkSession: QPKSession) {
            visibility = View.VISIBLE
        }

        /**
         * pk结束 隐藏
         */
        override fun onStop(pkSession: QPKSession, code: Int, msg: String) {
            visibility = View.GONE
        }

        override fun onStartTimeOut(pkSession: QPKSession) {
        }

    }

    override fun getLayoutId(): Int {
        return R.layout.kit_pk_cover_view
    }

    override fun initView() {
        client!!.getService(QPKService::class.java).addServiceListener(mQPKServiceListener)
    }

    override fun onDestroyed() {
        client!!.getService(QPKService::class.java).removeServiceListener(mQPKServiceListener)
        super.onDestroyed()
    }

}