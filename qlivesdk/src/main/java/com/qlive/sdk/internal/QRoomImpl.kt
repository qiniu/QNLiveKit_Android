package com.qlive.sdk.internal

import com.qlive.coreimpl.http.PageData
import com.qlive.core.been.QCreateRoomParam
import com.qlive.core.QLiveCallBack
import com.qlive.sdk.QRooms
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.coreimpl.getCode
import com.qlive.giftservice.QGift
import com.qlive.giftservice.QGiftStatistics
import com.qlive.giftservice.inner.GiftDataSource

internal class QRoomImpl private constructor() : QRooms {
    companion object {
        val instance = QRoomImpl()
    }

    private val dataSource = QLiveDataSource()
    private val giftDataSource = GiftDataSource()
    override fun createRoom(param: QCreateRoomParam, callBack: QLiveCallBack<QLiveRoomInfo>?) {
        backGround {
            doWork {
                val resp = dataSource.createRoom(param)
                callBack?.onSuccess(resp)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun deleteRoom(roomID: String, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                val resp = dataSource.deleteRoom(roomID)
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun listRoom(
        pageNumber: Int,
        pageSize: Int,
        callBack: QLiveCallBack<List<QLiveRoomInfo>>?
    ) {
        backGround {
            doWork {
                val resp = dataSource.listRoom(
                    pageNumber,
                    pageSize
                )
                callBack?.onSuccess(resp.list)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun liveRecord(
        pageNumber: Int,
        pageSize: Int,
        callBack: QLiveCallBack<List<QLiveRoomInfo>>?
    ) {
        backGround {
            doWork {
                val resp = dataSource.liveRecord(
                    pageNumber,
                    pageSize
                )
                callBack?.onSuccess(resp.list)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun getLiveStatistics(roomID: String, callBack: QLiveCallBack<QLiveStatistics>?) {
        backGround {
            doWork {
                val resp = dataSource.liveStatisticsGet(roomID)
                callBack?.onSuccess(resp)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun getRoomInfo(roomID: String, callBack: QLiveCallBack<QLiveRoomInfo>?) {
        backGround {
            doWork {
                val resp = dataSource.refreshRoomInfo(roomID)
                callBack?.onSuccess(resp)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }


    override fun getGiftConfig(type: Int, callback: QLiveCallBack<List<QGift>>?) {
        backGround {
            doWork {
                val gifts = giftDataSource.giftList(type, true)
                callback?.onSuccess(gifts)
            }
            catchError {
                callback?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun getLiveGiftStatistics(
        roomID: String,
        pageNumber: Int,
        pageSize: Int,
        callback: QLiveCallBack<List<QGiftStatistics>>?
    ) {
        backGround {
            doWork {
                val gifts = giftDataSource.roomGiftStatistics(roomID, pageNumber, pageSize)
                callback?.onSuccess(gifts)
            }
            catchError {
                callback?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun getAnchorGiftStatistics(
        pageNumber: Int,
        pageSize: Int,
        callback: QLiveCallBack<List<QGiftStatistics>>?
    ) {
        backGround {
            doWork {
                val gifts = giftDataSource.anchorGiftStatistics(pageNumber, pageSize)
                callback?.onSuccess(gifts)
            }
            catchError {
                callback?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun getUserGiftStatistics(
        pageNumber: Int,
        pageSize: Int,
        callback: QLiveCallBack<List<QGiftStatistics>>?
    ) {
        backGround {
            doWork {
                val gifts = giftDataSource.userGiftStatistics(pageNumber, pageSize)
                callback?.onSuccess(gifts)
            }
            catchError {
                callback?.onError(it.getCode(), it.message)
            }
        }
    }
}