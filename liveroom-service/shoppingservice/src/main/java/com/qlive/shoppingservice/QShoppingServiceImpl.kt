package com.qlive.shoppingservice

import android.text.TextUtils
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QExtension
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.coreimpl.*
import com.qlive.coreimpl.model.LiveStatistics
import com.qlive.jsonutil.JsonUtils
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg

internal class QShoppingServiceImpl : BaseService(), QShoppingService {

    private val mShoppingDataSource = ShoppingDataSource()
    private var mExplaining: QItem? = null
    private val mShoppingServiceListeners = ArrayList<QShoppingServiceListener>()
    private val ACTION_EXPLAINING = "liveroom_shopping_explaining"
    private val ACTION_EXTENSION = "liveroom_shopping_extension"
    private val ACTION_REFRESH = "liveroom_shopping_refresh"

    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            if (toID != currentRoomInfo?.chatID) {
                return false
            }
            if (msg.optAction() == ACTION_EXPLAINING) {
                var data = JsonUtils.parseObject(msg.optData(), QItem::class.java)
                if (TextUtils.isEmpty(data?.itemID)) {
                    data = null
                }
                mExplaining = data
                mShoppingServiceListeners.forEach {
                    it.onExplainingUpdate(data)
                }
                return true
            }

            if (msg.optAction() == ACTION_EXTENSION) {
                val data =
                    JsonUtils.parseObject(msg.optData(), QItemExtMsg::class.java) ?: return true
                mShoppingServiceListeners.forEach {
                    it.onExtensionUpdate(data.item, data.extension)
                }
                return true
            }
            if (msg.optAction() == ACTION_REFRESH) {
                mShoppingServiceListeners.forEach {
                    it.onItemListUpdate()
                }
                return true
            }
            return false
        }
    }

    private fun sendActionRefresh() {
        RtmManager.rtmClient.sendChannelMsg(
            RtmTextMsg<String>(ACTION_REFRESH, "").toJsonString(),
            currentRoomInfo?.chatID ?: "",
            false, object : RtmCallBack {
                override fun onSuccess() {
                }

                override fun onFailure(code: Int, msg: String) {
                }
            }
        )
    }

    init {
        RtmManager.addRtmChannelListener(mRtmMsgListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
        mShoppingServiceListeners.clear()
    }

    override fun getItemList(callBack: QLiveCallBack<List<QItem>>?) {
        backGround {
            doWork {
                val ret = mShoppingDataSource.getItemList(currentRoomInfo?.liveID ?: "")
                callBack?.onSuccess(ret)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 正在展示的商品 轮训同步状态
     */
    private val mItemShader = Scheduler(20000) {
        if (currentRoomInfo == null) {
            return@Scheduler
        }
        backGround {
            doWork {
                var newItem: QItem? = null
                try {
                    newItem = mShoppingDataSource.getExplaining(currentRoomInfo?.liveID ?: "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (!TextUtils.isEmpty(newItem?.itemID)) {
                    if (mExplaining == null || newItem!!.itemID != mExplaining?.itemID) {
                        mExplaining = newItem;
                        mShoppingServiceListeners.forEach {
                            it.onExplainingUpdate(mExplaining)
                        }
                        return@doWork
                    }
                } else {
                    if (mExplaining != null) {
                        mExplaining = null;
                        mShoppingServiceListeners.forEach {
                            it.onExplainingUpdate(null)
                        }
                    }
                }
            }
            catchError {

            }
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        if (mShoppingServiceListeners.size > 0) {
            mItemShader.start()
        }
    }

    override fun onLeft() {
        super.onLeft()
        mItemShader.cancel()
    }

    override fun updateItemStatus(
        itemID: String,
        status: QItemStatus,
        callBack: QLiveCallBack<Void>?
    ) {
        backGround {
            doWork {
                mShoppingDataSource.updateStatus(
                    currentRoomInfo?.liveID ?: "",
                    HashMap<String, Int>().apply {
                        put(itemID, status.value)
                    })
                sendActionRefresh()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun updateItemStatus(
        newStatus: java.util.HashMap<String, QItemStatus>,
        callBack: QLiveCallBack<Void>?
    ) {
        backGround {
            doWork {
                mShoppingDataSource.updateStatus(
                    currentRoomInfo?.liveID ?: "",
                    HashMap<String, Int>().apply {
                        newStatus.forEach {
                            put(it.key, it.value.value)
                        }
                    })
                sendActionRefresh()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun updateItemExtension(
        item: QItem,
        extension: QExtension,
        callBack: QLiveCallBack<Void>?
    ) {
        backGround {
            doWork {
                mShoppingDataSource.updateItemExtension(
                    currentRoomInfo?.liveID ?: "", item.itemID, extension
                )
                val msg = QItemExtMsg()
                msg.item = item
                msg.extension = extension
                RtmManager.rtmClient.sendChannelMsg(
                    RtmTextMsg<QItemExtMsg>(ACTION_EXTENSION, msg).toJsonString(),
                    currentRoomInfo?.chatID ?: "",
                    true
                )
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun setExplaining(item: QItem, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mShoppingDataSource.setExplaining(
                    currentRoomInfo?.liveID ?: "", item.itemID
                )
                try {
                    RtmManager.rtmClient.sendChannelMsg(
                        RtmTextMsg<QItem?>(ACTION_EXPLAINING, item).toJsonString(),
                        currentRoomInfo?.chatID ?: "",
                        false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mExplaining = item
                mShoppingServiceListeners.forEach {
                    it.onExplainingUpdate(mExplaining)
                }
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun cancelExplaining(callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mShoppingDataSource.cancelExplaining(
                    currentRoomInfo?.liveID ?: ""
                )
                try {
                    RtmManager.rtmClient.sendChannelMsg(
                        RtmTextMsg<QItem?>(ACTION_EXPLAINING, QItem()).toJsonString(),
                        currentRoomInfo?.chatID ?: "",
                        false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mExplaining = null
                mShoppingServiceListeners.forEach {
                    it.onExplainingUpdate(null)
                }
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun getExplaining(): QItem? {
        return mExplaining
    }

    override fun changeSingleOrder(param: QSingleOrderParam, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mShoppingDataSource.changeSingleOrder(
                    currentRoomInfo?.liveID ?: "",
                    param.itemID, param.from, param.to
                )
                sendActionRefresh()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun changeOrder(params: MutableList<QOrderParam>, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mShoppingDataSource.changeOrder(
                    currentRoomInfo?.liveID ?: "",
                    HashMap<String, Int>().apply {
                        params.forEach { p ->
                            put(p.itemID, p.order)
                        }
                    }
                )
                sendActionRefresh()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun startRecord(callBack: QLiveCallBack<Void>?) {
        if (mExplaining == null) {
            callBack?.onError(-1, "mExplaining==null")
            return
        }
        backGround {
            doWork {
                mShoppingDataSource.startRecord(
                    currentRoomInfo?.liveID ?: "",
                    mExplaining!!.itemID
                )
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun deleteRecord( recordIds: MutableList<Int>, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mShoppingDataSource.deleteRecord(
                    currentRoomInfo?.liveID ?: "",
                    recordIds
                )
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun statsQItemClick(item: QItem) {
        backGround {
            doWork {
                QLiveDataSource().liveStatisticsReq(listOf(LiveStatistics().apply {
                    type = QLiveStatistics.TYPE_QItem_CLICK_COUNT
                    live_id = currentRoomInfo?.liveID?:""
                    user_id = user?.userId?:""
                    biz_id = item.itemID
                    count = 1
                }))
            }
        }
    }

    override fun deleteItems(itemIDS: MutableList<String>, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mShoppingDataSource.deleteItems(
                    currentRoomInfo?.liveID ?: "",
                    itemIDS
                )
                sendActionRefresh()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun addServiceListener(listener: QShoppingServiceListener) {
        mShoppingServiceListeners.add(listener)
    }

    override fun removeServiceListener(listener: QShoppingServiceListener) {
        mShoppingServiceListeners.remove(listener)
    }
}