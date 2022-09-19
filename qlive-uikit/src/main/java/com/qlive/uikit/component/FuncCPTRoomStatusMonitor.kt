package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveStatus
import com.qlive.core.QLiveStatusListener
import com.qlive.uikit.R
import com.qlive.uikitcore.QLiveFuncComponent

/**
 * 房间销毁结束页面功能组件
 */
class FuncCPTRoomStatusMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mQLiveStatusListener = QLiveStatusListener { liveStatus -> //如果房主离线 关闭页面
        if (liveStatus == QLiveStatus.OFF) {
            Toast.makeText(
                kitContext?.androidContext,
                R.string.live_room_destroyed_tip,
                Toast.LENGTH_SHORT
            ).show()
            kitContext?.currentActivity?.finish()
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

/**
 * 房主掉线结束页面功能组件
 */
class FuncCPTAnchorStatusMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mQLiveStatusListener = QLiveStatusListener { liveStatus -> //如果房主离线 关闭页面
        if (liveStatus == QLiveStatus.ANCHOR_OFFLINE) {
            Toast.makeText(
                kitContext?.androidContext,
                R.string.live_anchor_offline_tip,
                Toast.LENGTH_SHORT
            ).show()
            kitContext?.currentActivity?.finish()
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
