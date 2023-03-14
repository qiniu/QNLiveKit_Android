package com.qlive.uikituser

import android.content.Context
import android.util.AttributeSet
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikitcore.QKitTextView
import com.qlive.uikitcore.Scheduler
import com.qlive.uikitcore.ext.toHtml
import java.text.DecimalFormat

//房间计时器
class RoomTimerView : QKitTextView {

    companion object{
        /**
         * 文本显示回调
         */
        var showTimeCall: ((time: Int) -> String) = {
            "<font color='#ffffff'>${formatTime(it)}</font>"
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

    private var total = 0;
    private val mScheduler = Scheduler(1000) {
        total++
        text = showTimeCall(total).toHtml()
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        super.onJoined(roomInfo,isJoinedBefore)
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

}
