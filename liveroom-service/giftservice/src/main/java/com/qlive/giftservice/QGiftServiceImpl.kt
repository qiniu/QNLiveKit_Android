package com.qlive.giftservice

import android.content.Context
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.coreimpl.BaseService
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.coreimpl.getCode
import com.qlive.giftservice.QGiftMsg.GIFT_ACTION
import com.qlive.giftservice.inner.GiftDataSource
import com.qlive.giftservice.inner.InnerGiftMsg
import com.qlive.jsonutil.JsonUtils
import com.qlive.rtm.RtmManager
import com.qlive.rtm.RtmMsgListener
import com.qlive.rtm.msg.TextMsg

internal class QGiftServiceImpl : QGiftService, BaseService() {


    private val liveDataSource = QLiveDataSource()
    private val giftDataSource = GiftDataSource()
    private val listeners = ArrayList<QGiftServiceListener>()
    private val mRtmMsgListener = object : RtmMsgListener {

        override fun onNewMsg(msg: TextMsg): Boolean {

            val action = msg.optAction()
            if (GIFT_ACTION == action && msg.toID == currentRoomInfo?.chatID) {
                val innerMsg =
                    JsonUtils.parseObject(msg.optData(), InnerGiftMsg::class.java) ?: return true
                backGround {
                    doWork {
                        val user = liveDataSource.searchUserByUserId(innerMsg.user_id)
                        val giftBeen = giftDataSource.giftByID(innerMsg.gift_id)
                        val giftMsg = QGiftMsg().apply {
                            liveID = innerMsg.live_id
                            gift = giftBeen
                            sender = user
                        }
                        listeners.forEach {
                            it.onReceivedGiftMsg(giftMsg)
                        }
                    }
                }
                return true
            }
            return false
        }
    }

    override fun sendGift(giftID: Int, amount: Int, callback: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                giftDataSource.sendGift(currentRoomInfo?.liveID ?: "", giftID, amount)
                callback?.onSuccess(null)
//                val giftMsg = QGiftMsg().apply {
//                    liveID = currentRoomInfo?.liveID?:""
//                    gift =  giftDataSource.giftByID(giftID)
//                    sender = user
//                }
//                listeners.forEach {
//                    it.onReceivedGiftMsg(giftMsg)
//                }
            }
            catchError {
                callback?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun addGiftServiceListener(listener: QGiftServiceListener) {
        listeners.add(listener)
    }

    override fun removeGiftServiceListener(listener: QGiftServiceListener) {
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