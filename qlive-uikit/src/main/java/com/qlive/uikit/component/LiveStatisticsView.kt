package com.qlive.uikit.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.qlive.chatservice.QChatRoomService
import com.qlive.chatservice.QChatRoomServiceListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.core.been.QLiveStatistics.TYPE_LIKE_COUNT
import com.qlive.core.been.QLiveStatistics.TYPE_LIVE_WATCHER_COUNT
import com.qlive.likeservice.QLike
import com.qlive.likeservice.QLikeService
import com.qlive.likeservice.QLikeServiceListener
import com.qlive.roomservice.QRoomService
import com.qlive.sdk.QLive
import com.qlive.uikitcore.QKitTextView
import com.qlive.uikitcore.Scheduler

class LiveStatisticsView : QKitTextView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var uv = 0
    private var onlineCount = 1
    private var like = 0

    private val mChatRoomServiceListener = object :
        QChatRoomServiceListener {
        override fun onUserJoin(memberID: String) {
            refreshOnline(true)
        }

        override fun onUserLeft(memberID: String) {
            refreshOnline(false)
        }

        override fun onUserKicked(memberID: String) {
            refreshOnline(false)
        }
    }
    private val mQLikeServiceListener = QLikeServiceListener {
        newLike()
    }
    private val mScheduler = Scheduler(60000) {
        if (roomInfo == null) {
            return@Scheduler
        }
        getStatistics(roomInfo!!.liveID)
    }

    private fun getStatistics(roomId: String) {
        val room = client?.getService(QRoomService::class.java)?.roomInfo
        onlineCount = (room?.onlineCount?.toInt()) ?: 1
        QLive.getRooms().getLiveStatistics(roomId, object : QLiveCallBack<QLiveStatistics> {
            override fun onError(code: Int, msg: String?) {

            }

            override fun onSuccess(data: QLiveStatistics) {
                data.info.forEach {
                    if (it.type == TYPE_LIVE_WATCHER_COUNT) {
                        uv = it.uniqueVisitor
                    }
                    if (it.type == TYPE_LIKE_COUNT) {
                        like = it.pageView
                    }
                }
                text = "${uv}浏览 ${onlineCount}在线 ${like}点赞"
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun refreshOnline(add: Boolean) {
        if (add) {
            onlineCount++
        } else {
            onlineCount--
        }
        text = "${uv}浏览 ${onlineCount}在线 ${like}点赞"
    }

    @SuppressLint("SetTextI18n")
    private fun newLike() {
        like++
        text = "${uv}浏览 ${onlineCount}在线 ${like}点赞"
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        mScheduler.start(true)
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        super.onGetLiveRoomInfo(roomInfo)
        getStatistics(roomInfo.liveID)
    }

    override fun onLeft() {
        super.onLeft()
        mScheduler.cancel()
    }

    override fun onDestroyed() {
        client?.getService(QChatRoomService::class.java)
            ?.removeServiceListener(mChatRoomServiceListener)
        client?.getService(QLikeService::class.java)
            ?.removeLikeServiceListener(mQLikeServiceListener)
        super.onDestroyed()
        mScheduler.cancel()
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QLikeService::class.java).addLikeServiceListener(mQLikeServiceListener)
        client.getService(QChatRoomService::class.java).addServiceListener(mChatRoomServiceListener)
    }
}