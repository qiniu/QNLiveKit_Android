package com.qlive.uikitpublicchat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.uikitcore.QKitTextView
import com.qlive.uikitcore.ext.toHtml
import kotlinx.coroutines.*

/**
 * 公告槽位
 */
class RoomNoticeView : QKitTextView {

    companion object{
        //显示公告
        var noticeHtmlShowAdapter: ((notice: String) -> String) = {
            "  <font color='#3ce1ff'>官方公告</font>" + " <font color='#ffb83c'>" + ":${it}</font>";
        }
    }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var goneJob: Job? = null
    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo,isResumeUIFromFloating)
        goneJob?.cancel()
        text = noticeHtmlShowAdapter.invoke(roomInfo.notice?:"").toHtml()
        if (text.isEmpty()) {
            visibility = View.INVISIBLE
            return
        }
        visibility = View.VISIBLE
        goneJob = kitContext?.lifecycleOwner?.lifecycleScope?.launch(Dispatchers.Main) {
            try {
                delay(1000 * 60)
                visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}