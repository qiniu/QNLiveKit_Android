package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveStatus
import com.qlive.core.QLiveStatusListener
import com.qlive.core.anchorStatusToLiveStatus
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikit.R
import com.qlive.uikitcore.QKitTextView
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.ext.isTrailering
import java.text.SimpleDateFormat
import java.util.*

class AnchorOfflineTipView : QKitTextView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun attachKitContext(context: QLiveUIKitContext) {
        super.attachKitContext(context)
        visibility = GONE
    }

    private val mQLiveStatusListener = QLiveStatusListener { liveStatus, _ -> //如果房主离线 关闭页面
        if (liveStatus == QLiveStatus.ANCHOR_OFFLINE) {
            visibility = View.VISIBLE
            text = context.getString(R.string.live_anchor_offline_tip)
            kitContext?.getPlayerRenderViewCall?.invoke()?.view?.visibility = View.INVISIBLE
        } else {
            visibility = View.GONE
            text = ""
            kitContext?.getPlayerRenderViewCall?.invoke()?.view?.visibility = View.VISIBLE
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        super.onJoined(roomInfo, isJoinedBefore)
        if (roomInfo.isTrailering()) {
            visibility = View.VISIBLE
            val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
            val d1 = Date(roomInfo.startTime * 1000)
            val timeFormat: String = format.format(d1)
            text = String.format(context.getString(R.string.live_room_trailer_tip), timeFormat)
            return
        }
        if (roomInfo.anchorStatus.anchorStatusToLiveStatus() == QLiveStatus.ANCHOR_OFFLINE) {
            visibility = View.VISIBLE
            text = context.getString(R.string.live_anchor_offline_tip)
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.addLiveStatusListener(mQLiveStatusListener)
    }

    override fun onDestroyed() {
        client?.removeLiveStatusListener(mQLiveStatusListener)
        super.onDestroyed()
    }
}