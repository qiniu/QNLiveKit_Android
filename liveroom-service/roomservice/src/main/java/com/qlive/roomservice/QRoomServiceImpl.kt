package com.qlive.roomservice

import com.qlive.rtm.RtmManager
import com.qlive.rtm.sendChannelMsg
import com.qlive.jsonutil.JsonUtils
import com.qlive.coreimpl.model.LiveIdExtensionMode
import com.qlive.coreimpl.datesource.RoomDataSource
import com.qlive.coreimpl.datesource.UserDataSource
import com.qlive.coreimpl.util.backGround
import com.qlive.core.*
import com.qlive.core.been.QExtension
import com.qlive.coreimpl.BaseService
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.util.getCode
import java.lang.Exception
import java.util.*

internal class QRoomServiceImpl : BaseService(), QRoomService {

    private val roomDataSource = RoomDataSource()
    private val userDataSource = UserDataSource()

    private val mQRoomServiceListeners = LinkedList<QRoomServiceListener>()

    override fun addRoomServiceListener(listener: QRoomServiceListener) {
        mQRoomServiceListeners.add(listener)
    }

    override fun removeRoomServiceListener(listener: QRoomServiceListener) {
        mQRoomServiceListeners.remove(listener)
    }

    /**
     * 获取当前房间
     *
     * @return
     */
    override fun getRoomInfo(): QLiveRoomInfo? {
        return currentRoomInfo
    }

    /**
     * 刷新房间信息
     */
    override fun getRoomInfo(callBack: QLiveCallBack<QLiveRoomInfo>?) {
        backGround {
            doWork {
                val room = roomDataSource.refreshRoomInfo(currentRoomInfo?.liveID ?: "")
                currentRoomInfo = room
                callBack?.onSuccess(currentRoomInfo)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 跟新直播扩展信息
     *
     * @param extension
     */
    override fun updateExtension(extension: QExtension, callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                roomDataSource.updateRoomExtension(currentRoomInfo?.liveID ?: "", extension)
                callBack?.onSuccess(null)
                try {
                    val mode = LiveIdExtensionMode()
                    mode.liveId = currentRoomInfo?.liveID ?: ""
                    mode.extension = extension
                    RtmManager.rtmClient.sendChannelMsg(
                        JsonUtils.toJson(mode),
                        currentRoomInfo?.chatID ?: "",
                        true
                    )
                } catch (e: Exception) {
                }
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 当前房间在线用户
     *
     * @param callBack
     */
    override fun getOnlineUser(
        page_num: Int,
        page_size: Int,
        callBack: QLiveCallBack<List<QLiveUser>>?
    ) {
        backGround {
            doWork {
                val users =
                    userDataSource.getOnlineUser(currentRoomInfo?.liveID ?: "", page_num, page_size)
                callBack?.onSuccess(users.list)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 某个房间在线用户
     *
     * @param callBack
     */
    override fun getOnlineUser(
        pageNum: Int,
        pageSize: Int,
        roomId: String,
        callBack: QLiveCallBack<List<QLiveUser>>?
    ) {

        backGround {
            doWork {
                val users =
                    userDataSource.getOnlineUser(roomId, pageNum, pageSize)
                callBack?.onSuccess(users.list)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 使用用户ID搜索房间用户
     *
     * @param uid
     * @param callBack
     */
    override fun searchUserByUserId(uid: String, callBack: QLiveCallBack<QLiveUser>?) {
        backGround {
            doWork {
                val users =
                    userDataSource.searchUserByUserId(uid)
                callBack?.onSuccess(users)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 使用用户im uid 搜索用户
     * @param imUid
     * @param callBack
     */
    override fun searchUserByIMUid(imUid: String, callBack: QLiveCallBack<QLiveUser>?) {
        backGround {
            doWork {
                val users =
                    userDataSource.searchUserByIMUid(imUid)
                callBack?.onSuccess(users)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mQRoomServiceListeners.clear()
    }
}