package com.qlive.pubchatservice

import android.content.Context
import android.text.TextUtils
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.coreimpl.QLiveServiceObserver
import com.qlive.core.been.QLiveStatistics
import com.qlive.coreimpl.BaseService
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.coreimpl.model.LiveStatistics
import com.qlive.jsonutil.JsonUtils
import com.qlive.pubchatservice.QPublicChat.action_puchat
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg
import com.qlive.rtm.msg.TextMsg
import java.util.*

internal class QPublicChatServiceImpl : QPublicChatService, BaseService() {

    private val mListeners = LinkedList<QPublicChatServiceLister>()
    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: TextMsg): Boolean {
            if (msg.toID != currentRoomInfo?.chatID) {
                return false
            }
            if (
                msg.optAction() == QPublicChat.action_welcome ||
                msg.optAction() == QPublicChat.action_bye ||
                msg.optAction() == QPublicChat.action_like ||
                msg.optAction() == QPublicChat.action_puchat ||
                msg.optAction() == QPublicChat.action_pubchat_custom
            ) {
                val mode =
                    JsonUtils.parseObject(msg.optData(), QPublicChat::class.java) ?: return true
                mode.msgID = msg.msgID
                if (msg.optAction() == QPublicChat.action_welcome
                    && client is QLiveServiceObserver
                ) {
                    (client as QLiveServiceObserver?)?.notifyUserJoin(mode.sendUser?.userId ?: "")
                }
                if (msg.optAction() == QPublicChat.action_bye
                    && client is QLiveServiceObserver
                ) {
                    (client as QLiveServiceObserver?)?.notifyUserLeft(mode.sendUser?.userId ?: "")
                }
                mListeners.forEach {
                    it.onReceivePublicChat(mode)
                }
                return true
            }
            return false
        }
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        RtmManager.addRtmChannelListener(mRtmMsgListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mListeners.clear()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
    }

    private fun sendModel(model: QPublicChat, callBack: QLiveCallBack<QPublicChat>?) {
        val msg = RtmTextMsg(model.action, model).toJsonString()

        RtmManager.rtmClient.sendChannelTextMsg(
            msg,
            currentRoomInfo?.chatID ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(model)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }

    /**
     * 发送 聊天室聊天
     * @param msg
     */
    override fun sendPublicChat(msg: String, callBack: QLiveCallBack<QPublicChat>?) {
        sendModel(QPublicChat().apply {
            action = QPublicChat.action_puchat
            sendUser = user
            content = msg
            senderRoomId = currentRoomInfo?.liveID
        }, callBack)
        backGround {
            doWork {
                QLiveDataSource().liveStatisticsReq(listOf(LiveStatistics().apply {
                    type = QLiveStatistics.TYPE_PUBCHAT_COUNT
                    live_id = currentRoomInfo?.liveID ?: ""
                    user_id = user?.userId ?: ""
                    count = 1
                }))
            }
        }
    }

    override fun getHistoryChatMsg(
        startMsgID: String,
        size: Int, callBack: QLiveCallBack<MutableList<QPublicChat>>
    ) {
        val id = if (TextUtils.isEmpty(startMsgID)) {
            -1
        } else {
            startMsgID.toLong()
        }
        RtmManager.rtmClient.getHistoryTextMsg(currentRoomInfo?.chatID ?: "-1", id, size,
            object : RtmDadaCallBack<List<TextMsg>> {
                override fun onSuccess(data: List<TextMsg>) {
                    val list = LinkedList<QPublicChat>()
                    data.forEach { msg ->
                        if (
                            msg.optAction() == QPublicChat.action_welcome ||
                            msg.optAction() == QPublicChat.action_bye ||
                            msg.optAction() == QPublicChat.action_like ||
                            msg.optAction() == QPublicChat.action_puchat ||
                            msg.optAction() == QPublicChat.action_pubchat_custom
                        ) {
                            val mode = JsonUtils.parseObject(msg.optData(), QPublicChat::class.java)
                            mode?.let {
                                it.msgID = msg.msgID
                                list.add(it)
                            }
                        }
                    }
                    callBack.onSuccess(list)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack.onError(code, msg)
                }
            })
    }

    /**
     * 发送 欢迎进入消息
     *
     * @param msg
     */
    override fun sendWelCome(msg: String, callBack: QLiveCallBack<QPublicChat>?) {
        sendModel(QPublicChat().apply {
            action = QPublicChat.action_welcome
            sendUser = user
            content = msg
            senderRoomId = currentRoomInfo?.liveID
        }, callBack)
    }

    /**
     * 发送 拜拜
     *
     * @param msg
     */
    override fun sendByeBye(msg: String, callBack: QLiveCallBack<QPublicChat>?) {
        sendModel(QPublicChat().apply {
            action = QPublicChat.action_bye
            sendUser = user
            content = msg
            senderRoomId = currentRoomInfo?.liveID
        }, callBack)
    }

    /**
     * 点赞
     *
     * @param msg
     * @param callBack
     */
    override fun sendLike(msg: String, callBack: QLiveCallBack<QPublicChat>?) {
        sendModel(QPublicChat().apply {
            action = QPublicChat.action_like
            sendUser = user
            content = msg
            senderRoomId = currentRoomInfo?.liveID
        }, callBack)
    }

    /**
     * 自定义要显示在公屏上的消息
     *
     * @param action
     * @param msg
     * @param extensions
     * @param callBack
     */
    override fun sendCustomPubChat(
        act: String,
        msg: String,
        callBack: QLiveCallBack<QPublicChat>?
    ) {
        val mode = QPublicChat().apply {
            action = act
            sendUser = user
            content = msg
            senderRoomId = currentRoomInfo?.liveID
        }

        val rtmmsg = RtmTextMsg(
            QPublicChat.action_pubchat_custom,
            mode
        ).toJsonString()
        RtmManager.rtmClient.sendChannelCMDMsg(
            rtmmsg,
            currentRoomInfo?.chatID ?: "",
            false,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(mode)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }

    /**
     * 往本地公屏插入消息 不发送到远端
     */
    override fun pubMsgToLocal(chatModel: QPublicChat) {
        mListeners.forEach {
            it.onReceivePublicChat(chatModel)
        }
    }

    override fun addServiceLister(lister: QPublicChatServiceLister) {
        mListeners.add(lister)
    }

    override fun removeServiceLister(lister: QPublicChatServiceLister) {
        mListeners.remove(lister)
    }

}