package com.qlive.uikit.component

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikit.RoomPushActivity
import com.qlive.uikit.databinding.KitViewAnchorStartReserveLiveBinding
import com.qlive.uikitcore.QKitViewBindingFrameMergeLayout
import com.qlive.uikitcore.Scheduler
import com.qlive.uikitcore.ext.isTrailering
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitcore.ext.toHtml
import java.text.DecimalFormat

/**
 * Anchor start trailer live view
 *
 * @constructor Create empty Anchor start trailer live view
 */
class AnchorStartTrailerLiveView :
    QKitViewBindingFrameMergeLayout<KitViewAnchorStartReserveLiveBinding> {
    companion object {
        /**
         * 文本显示回调
         */
        var showTimeCall: ((time: Int) -> String) = {
            "<font color='#ffffff'>直播中 ${formatTime(it)}</font>"
        }

        private fun formatTime(time: Int): String {
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
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        super.onGetLiveRoomInfo(roomInfo)
        val roomId = kitContext!!.currentActivity.intent.getStringExtra(RoomPushActivity.KEY_ROOM_ID) ?: ""
        if (roomInfo.isTrailering()&&roomId.isNotEmpty()) {
            binding.tvStartReserve.visibility = VISIBLE
            binding.tvTime.visibility = GONE
        }
    }

    private var total = 0;
    private val mScheduler = Scheduler(1000) {
        total++
        binding.tvTime.text = showTimeCall(total).toHtml()
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        super.onJoined(roomInfo, isJoinedBefore)
        binding.tvStartReserve.visibility = GONE
        binding.tvTime.visibility = VISIBLE
        mScheduler.start()
    }

    override fun onLeft() {
        super.onLeft()
        mScheduler.cancel()
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mScheduler.cancel()
    }

    override fun initView() {
        binding.tvTime.typeface = Typeface.MONOSPACE
        binding.tvStartReserve.setDoubleCheckClickListener {
            kitContext?.startPusherRoomActionCall?.invoke(
                null,
                object : QLiveCallBack<QLiveRoomInfo> {
                    override fun onError(code: Int, msg: String?) {}
                    override fun onSuccess(data: QLiveRoomInfo?) {}
                })
        }
    }
}