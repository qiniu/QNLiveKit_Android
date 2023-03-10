package com.qlive.uikitpk

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.qlive.core.been.QExtension
import com.qlive.pkservice.QPKService
import com.qlive.pkservice.QPKServiceListener
import com.qlive.pkservice.QPKSession

import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.Scheduler

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

    override fun attachKitContext(context: QLiveUIKitContext) {
        super.attachKitContext(context)
      //  visibility = View.GONE
    }
    companion object {
        //pk总时长
        const val KEY_TOTAL_DURATION = "TotalDuration"

        //记分阶段时长
        const val KEY_PK_DURATION = "pkDuration"

        //惩罚阶段时长
        const val KEY_PENALTY_DURATION = "penaltyDuration"

        //pk分数事件
        const val KEY_PK_INTEGRAL = "pkIntegral"

        //输赢事件
        const val PK_DURATION = "pkWinOrLose"

    }

    private val pkTimer = Scheduler(1000) {

    }

    private val mQPKServiceListener = object : QPKServiceListener {

        /**
         * pk开始 显示
         */
        override fun onStart(pkSession: QPKSession) {
            visibility = View.VISIBLE
            pkTimer.start()
            pkSession.extension?.entries?.forEach {
                setPKInfo(it.key, it.value)
            }
        }

        /**
         * pk结束 隐藏
         */
        override fun onStop(pkSession: QPKSession, code: Int, msg: String) {
            visibility = View.GONE
            pkTimer.cancel()
        }

        override fun onStartTimeOut(pkSession: QPKSession) {
        }

        override fun onPKExtensionChange(extension: QExtension) {
            super.onPKExtensionChange(extension)
            setPKInfo(extension.key, extension.value)
        }

    }

    private fun setPKInfo(key: String, value: String) {
        when (key) {
            KEY_PK_INTEGRAL -> {

            }
            PK_DURATION -> {

            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_pk_cover_view
    }

    override fun initView() {
        client!!.getService(QPKService::class.java).addServiceListener(mQPKServiceListener)
    }

    override fun onDestroyed() {
        pkTimer.cancel()
        client!!.getService(QPKService::class.java).removeServiceListener(mQPKServiceListener)
        super.onDestroyed()
    }

}