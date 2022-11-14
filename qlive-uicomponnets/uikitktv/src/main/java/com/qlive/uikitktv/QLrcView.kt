package com.qlive.uikitktv

import android.content.Context
import android.util.AttributeSet
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.ktvservice.QKTVMusic
import com.qlive.ktvservice.QKTVMusic.track_lrc
import com.qlive.ktvservice.QKTVService
import com.qlive.ktvservice.QKTVServiceListener
import com.qlive.uikitcore.QLiveComponent
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitktv.lrcview.LrcLoadUtils
import com.qlive.uikitktv.lrcview.LrcView
import com.qlive.uikitktv.lrcview.bean.LrcData
import java.io.File

class QLrcView : LrcView, QLiveComponent {

    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mKTVServiceListener = object : QKTVServiceListener {
        override fun onError(errorCode: Int, msg: String?) {}
        override fun onStart(ktvMusic: QKTVMusic) {
            reset()
            setLabel("歌词加载中")
            val url = ktvMusic.tracks[track_lrc] ?: return
            val file = File(url)
            val data: LrcData? = LrcLoadUtils.parse(file)
            setLrcData(data)
        }

        override fun onSwitchTrack(track: String?) {}
        override fun onPause() {}
        override fun onResume() {}
        override fun onStop() {}
        override fun onPositionUpdate(position: Long, duration: Long) {
            updateTime(position)
        }
        override fun onPlayCompleted() {}
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QKTVService::class.java).addKTVServiceListener(mKTVServiceListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        client?.getService(QKTVService::class.java)?.removeKTVServiceListener(mKTVServiceListener)
    }

}