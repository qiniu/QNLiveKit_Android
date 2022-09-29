package com.qlive.likeservice

import android.content.Context
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.coreimpl.BaseService
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.coreimpl.getCode
import com.qlive.jsonutil.JsonUtils
import com.qlive.likeservice.inner.InnerLike
import com.qlive.likeservice.inner.LikeDataSource
import com.qlive.rtm.RtmManager
import com.qlive.rtm.RtmMsgListener
import com.qlive.rtm.optAction

class QLikeServiceImpl : QLikeService, BaseService() {
    companion object {
        private const val LIKE_ACTION = "like_notify"
    }

    private val liveDataSource = QLiveDataSource()
    private val likeDataSource = LikeDataSource()
    private val listeners = ArrayList<QLikeServiceListener>()
    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            val action = msg.optAction()
            if (LIKE_ACTION == action && toID == currentRoomInfo?.chatID) {
                val msgBeen =
                    JsonUtils.parseObject(msg.optAction(), InnerLike::class.java) ?: return true
                backGround {
                    doWork {
                        val user = liveDataSource.searchUserByUserId(msgBeen.user_id)
                        val like = QLike().apply {
                            liveID = msgBeen.live_id
                            count = msgBeen.count
                            sender = user
                        }
                        listeners.forEach {
                            it.onReceivedLikeMsg(like)
                        }
                    }
                }
                return true
            }
            return false
        }
    }

    override fun like(count: Int, callback: QLiveCallBack<QLikeResponse>?) {
        backGround {
            doWork {
                val ret = likeDataSource.like(currentRoomInfo?.liveID ?: "", count)
                callback?.onSuccess(ret)
            }
            catchError {
                callback?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun addLikeServiceListener(listener: QLikeServiceListener) {
        listeners.add(listener)
    }

    override fun removeLikeServiceListener(listener: QLikeServiceListener?) {
        listeners.remove(listener)
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        RtmManager.addRtmChannelListener(mRtmMsgListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        listeners.clear()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
    }
}