package com.qlive.roomservice

import android.content.Context
import com.qlive.jsonutil.JsonUtils
import com.qlive.coreimpl.model.LiveIdExtensionMode
import com.qlive.core.*
import com.qlive.core.been.QExtension
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.*
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg
import java.lang.Exception
import java.util.*

internal class QRoomServiceImpl : BaseService(), QRoomService {
    companion object {
        private const val liveroom_extension_change = "liveroom_extension_change"
        private const val censor_notify = "censor_notify"
        private const val censor_stop = "censor_stop"
    }

    private val roomDataSource = QLiveDataSource()

    private val mQRoomServiceListeners = LinkedList<QRoomServiceListener>()

    private val mRtmListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            val action = msg.optAction()
            when (action) {
                liveroom_extension_change -> {
                    if (toID !== roomInfo?.chatID) {
                        return true
                    }
                    val data = JsonUtils.parseObject(msg.optData(), LiveIdExtensionMode::class.java)
                        ?: return true
                    mQRoomServiceListeners.forEach {
                        it.onRoomExtensionUpdate(data.extension)
                    }
                    return true
                }
                censor_notify -> {
                    if (toID != user?.imUid) {
                        return true
                    }
                    val data = JsonUtils.parseObject(msg.optData(), Censor::class.java)
                        ?: return true
                    if (data.live_id != currentRoomInfo?.liveID) {
                        return true
                    }
                    mQRoomServiceListeners.forEach {
                        it.onReceivedCensorNotify(data.message)
                    }
                    return true
                }
                censor_stop -> {
                    if (toID !== roomInfo?.chatID) {
                        return true
                    }
                    val data = JsonUtils.parseObject(msg.optData(), Censor::class.java)
                        ?: return true
                    if (client is QLiveServiceObserver) {
                        (client as QLiveServiceObserver?)?.notifyCheckStatus(
                            QLiveStatus.FORCE_CLOSE,
                            data.message
                        )
                    }
                    return true
                }
            }
            return false
        }
    }


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
        return currentRoomInfo?.clone()
    }

    /**
     * 刷新房间信息
     */
    override fun getRoomInfo(callBack: QLiveCallBack<QLiveRoomInfo>?) {
        backGround {
            doWork {
                val room = roomDataSource.refreshRoomInfo(currentRoomInfo?.liveID ?: "")
                currentRoomInfo?.copyRoomInfo(room)
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
                    RtmManager.rtmClient.sendChannelCMDMsg(
                        RtmTextMsg<LiveIdExtensionMode>(
                            liveroom_extension_change,
                            mode
                        ).toJsonString(),
                        currentRoomInfo?.chatID ?: "",
                        true
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
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
                    roomDataSource.getOnlineUser(currentRoomInfo?.liveID ?: "", page_num, page_size)
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
                    roomDataSource.getOnlineUser(roomId, pageNum, pageSize)
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
                    roomDataSource.searchUserByUserId(uid)
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
                val users = roomDataSource.searchUserByIMUid(imUid)
                callBack?.onSuccess(users)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        RtmManager.addRtmChannelListener(mRtmListener)
        RtmManager.addRtmC2cListener(mRtmListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mQRoomServiceListeners.clear()
        RtmManager.removeRtmChannelListener(mRtmListener)
        RtmManager.removeRtmC2cListener(mRtmListener)
    }

}