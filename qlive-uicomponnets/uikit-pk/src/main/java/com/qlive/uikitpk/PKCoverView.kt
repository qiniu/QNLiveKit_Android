package com.qlive.uikitpk

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QExtension
import com.qlive.jsonutil.JsonUtils
import com.qlive.liblog.QLiveLogUtil
import com.qlive.pkservice.QPKService
import com.qlive.pkservice.QPKServiceListener
import com.qlive.pkservice.QPKSession
import com.qlive.sdk.QLive

import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.uikitcore.QKitViewBindingFrameLayout
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.Scheduler
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitpk.databinding.KitPkCoverViewBinding
import java.text.DecimalFormat

/**
 * PK覆盖层 暂无UI
 */
class PKCoverView : QKitViewBindingFrameLayout<KitPkCoverViewBinding> {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun attachKitContext(context: QLiveUIKitContext) {
        super.attachKitContext(context)
        visibility = View.GONE
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
        const val PK_WIN_OR_LOSE = "pkWinOrLose"

    }

    private fun formatTime(time: Long): String {
        val decimalFormat = DecimalFormat("00")
        val hh: String = decimalFormat.format(time / 3600)
        val mm: String = decimalFormat.format(time % 3600 / 60)
        val ss: String = decimalFormat.format(time % 60)
        return if (hh == "00") {
            "$mm:$ss"
        } else {
            "$hh:$mm:$ss"
        }
    }

    private val pkTimer = Scheduler(1000) {
        val session =
            client?.getService(QPKService::class.java)?.currentPKingSession() ?: return@Scheduler
        val pkStartTime = session.startTimeStamp * 1000
        session.extension ?: return@Scheduler
        val duration = session.extension[KEY_PK_DURATION] ?: return@Scheduler
        val penaltyDuration = session.extension[KEY_PENALTY_DURATION] ?: return@Scheduler

        val now = System.currentTimeMillis()
        val durationTime = pkStartTime.toLong() + duration.toLong() * 1000
        val penaltyDurationTime = durationTime + penaltyDuration.toLong() * 1000

        var isInPenaltyDurationTime = false
        val timer = if (now > durationTime) {
            isInPenaltyDurationTime = true
            penaltyDurationTime - now
        } else {
            durationTime - now
        }
        if (timer <= 0) {
            if (isInPenaltyDurationTime && QLive.getLoginUser().userId == roomInfo?.anchor?.userId) {
                client?.getService(QPKService::class.java)?.stop(null)
            }
            return@Scheduler
        }

        val timeStr = if (isInPenaltyDurationTime) {
            "pk惩罚 ${formatTime(timer / 1000)}"
        } else {
            "倒计时 ${formatTime(timer / 1000)}"
        }
        binding.tvTimer.text = timeStr
    }

    private val mQPKServiceListener = object : QPKServiceListener {

        /**
         * pk开始 显示
         */
        override fun onStart(pkSession: QPKSession) {
            Log.d("setPKInfo", " 开始 ${JsonUtils.toJson(pkSession)} ")
            visibility = View.VISIBLE
            pkTimer.start()
            binding.pkProgressBar.setProgress(50)
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
            binding.pkProgressBar.setProgress(50)
            binding.pkProgressBar.setRightText("")
            binding.pkProgressBar.setLeftText("")
        }

        override fun onStartTimeOut(pkSession: QPKSession) {

        }

        override fun onPKExtensionChange(extension: QExtension) {
            super.onPKExtensionChange(extension)
            Log.d("floo_log","onPKExtensionChange"+extension.key+" "+extension.value)
            setPKInfo(extension.key, extension.value)
        }

    }

    private fun setPKInfo(key: String, value: String) {
        when (key) {
            KEY_PK_INTEGRAL -> {

                val pkIntegral = JsonUtils.parseObject(value, PKIntegral::class.java) ?: return
                var left = 0.1
                var right = 0.1
                if (pkIntegral.init_room_id == roomInfo?.liveID) {
                    left = pkIntegral.init_score.toDouble()
                    right = pkIntegral.recv_score.toDouble()
                } else {
                    right = pkIntegral.init_score.toDouble()
                    left = pkIntegral.recv_score.toDouble()
                }
                var leftTemp = left
                var rightTemp = right
                if (left <= 0) {
                    leftTemp = 0.1
                }
                if (right <= 0) {
                    rightTemp = 0.1
                }
                binding.pkProgressBar.setLeftText("我方${left}")
                binding.pkProgressBar.setRightText("对方${right}")
                binding.pkProgressBar.setProgress((leftTemp / (leftTemp + rightTemp) * 100).toInt())
            }

            PK_WIN_OR_LOSE -> {
                //pk输赢
                val pkResult = JsonUtils.parseObject(value, PKWinOrLose::class.java) ?: return
                QLiveLogUtil.d("pk输赢 ${value}")
            }
        }
    }

    var i=0
    override fun initView() {
        client!!.getService(QPKService::class.java).addServiceListener(mQPKServiceListener)
        binding.tvTimer.setOnClickListener {
            client!!.getService(QPKService::class.java).updateExtension(QExtension().apply {
                key="aa"
                value="222 sss ${i++}"
            },object: QLiveCallBack<Void>{
                override fun onError(code: Int, msg: String?) {

                }

                override fun onSuccess(data: Void?) {
                    "c".asToast(context)
                }

            })
        }
    }

    override fun onDestroyed() {
        pkTimer.cancel()
        client!!.getService(QPKService::class.java).removeServiceListener(mQPKServiceListener)
        super.onDestroyed()
    }

}