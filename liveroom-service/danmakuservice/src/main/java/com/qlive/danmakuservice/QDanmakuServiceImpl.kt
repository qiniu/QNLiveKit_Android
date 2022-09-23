package com.qlive.danmakuservice

import android.content.Context
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg
import com.qlive.jsonutil.JsonUtils
import com.qlive.coreimpl.BaseService
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveStatistics
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.coreimpl.model.LiveStatistics

internal class QDanmakuServiceImpl : QDanmakuService, BaseService() {
    private val roomDataSource = QLiveDataSource()
    private val mDanmakuServiceListeners = ArrayList<QDanmakuServiceListener>()
    private val rtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            if (toID != currentRoomInfo?.chatID) {
                return false
            }
            if (msg.optAction() == QDanmaku.action_danmu) {
                val mode = JsonUtils.parseObject(msg.optData(), QDanmaku::class.java) ?: return true
                mDanmakuServiceListeners.forEach {
                    it.onReceiveDanmaku(mode)
                }
                return true
            }
            return false
        }
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        RtmManager.addRtmChannelListener(rtmMsgListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mDanmakuServiceListeners.clear()
        RtmManager.removeRtmChannelListener(rtmMsgListener)
    }

    override fun addDanmakuServiceListener(listener: QDanmakuServiceListener) {
        mDanmakuServiceListeners.add(listener)
    }

    override fun removeDanmakuServiceListener(listener: QDanmakuServiceListener) {
        mDanmakuServiceListeners.remove(listener)
    }

    /**
     * 发送弹幕消息
     */
    override fun sendDanmaku(
        msg: String,
        extensions: HashMap<String, String>?,
        callBack: QLiveCallBack<QDanmaku>?
    ) {
        val mode = QDanmaku().apply {
            sendUser = user
            content = msg
            senderRoomID = currentRoomInfo?.liveID
            this.extension = extensions
        }
        val rtmMsg = RtmTextMsg<QDanmaku>(
            QDanmaku.action_danmu,
            mode
        )
        RtmManager.rtmClient.sendChannelMsg(rtmMsg.toJsonString(),
            currentRoomInfo?.chatID ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(mode)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
        backGround {
            doWork {
                roomDataSource.liveStatisticsReq(listOf(LiveStatistics().apply {
                    type = QLiveStatistics.TYPE_PUBCHAT_COUNT
                    live_id = currentRoomInfo?.liveID ?: ""
                    user_id = user?.userId ?: ""
                    count = 1
                }))
            }
        }
    }
}