package com.qlive.sdk.internal

import com.qlive.avparam.QIPlayer
import com.qlive.avparam.QPlayerEventListener
import com.qlive.avparam.QPlayerProvider
import com.qlive.avparam.QPlayerRenderView
import com.qlive.core.*
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.coreimpl.*
import com.qlive.playerclient.QPlayerClient
import com.qlive.qplayer.QMediaPlayer
import com.qlive.rtm.RtmManager
import com.qlive.rtm.joinChannel
import com.qlive.rtm.leaveChannel
import com.qlive.sdk.QLive
import com.qlive.sdk.internal.AppCache.Companion.appContext

internal class QPlayerClientImpl : QPlayerClient,
    QPlayerProvider,
    QLiveServiceObserver,
    LinkRoleObserver {
    companion object {
        fun create(): QPlayerClient {
            return QPlayerClientImpl()
        }
    }

    private val roomDataSource = QLiveDataSource()

    private val mMediaPlayer by lazy {
        QMediaPlayer(appContext)
    }

    private var mPlayerRenderView: QPlayerRenderView? = null
    private var mLiveStatusListeners = ArrayList<QLiveStatusListener>()
    private val mLiveContext by lazy {
        QNLiveRoomContext(this).apply {
            roomStatusChange = { status, msg ->
                if (status == QLiveStatus.ANCHOR_ONLINE) {
                    mMediaPlayer.start()
                }
                mLiveStatusListeners.forEach {
                    it.onLiveStatusChanged(status, msg)
                }
            }
        }
    }

    /**
     * 获取服务实例
     *
     * @param serviceClass
     * @param <T>
     * @return
    </T> */
    override fun <T : QLiveService> getService(serviceClass: Class<T>): T? {
        return mLiveContext.getService(serviceClass)
    }

    override fun addLiveStatusListener(liveStatusListener: QLiveStatusListener) {
        mLiveStatusListeners.add(liveStatusListener)
    }

    override fun removeLiveStatusListener(liveStatusListener: QLiveStatusListener?) {
        mLiveStatusListeners.remove(liveStatusListener)
    }

    /**
     * 加入房间
     * @param liveId
     * @param callBack
     */
    override fun joinRoom(liveId: String, callBack: QLiveCallBack<QLiveRoomInfo>?) {
        backGround {
            doWork {
                mLiveContext.enter(liveId, QLive.getLoginUser())
                val roomInfo = roomDataSource.joinRoom(liveId)
                if (RtmManager.isInit) {
                    RtmManager.rtmClient.joinChannel(roomInfo.chatID)
                }
                mLiveContext.joinedRoom(roomInfo)
                //设置播放地址
                mMediaPlayer.setUp(roomInfo.rtmpURL, null)
                if (mPlayerRenderView != null) {
                    //开始拉流
                    mMediaPlayer.start()
                }
                callBack?.onSuccess(roomInfo)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 离开房间
     * @param callBack
     */
    override fun leaveRoom(callBack: QLiveCallBack<Void>?) {
        backGround {
            doWork {
                mLiveContext.beforeLeaveRoom()
                roomDataSource.leaveRoom(mLiveContext.roomInfo?.liveID ?: "")
                if (RtmManager.isInit) {
                    RtmManager.rtmClient.leaveChannel(mLiveContext.roomInfo?.chatID ?: "")
                }
                mLiveContext.leaveRoom()
                mMediaPlayer.stop()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    override fun destroy() {
        mLiveStatusListeners.clear()
        mMediaPlayer.release()
        mLiveContext.destroy()
        mPlayerRenderView = null
    }

    override fun play(renderView: QPlayerRenderView) {
        this.mPlayerRenderView = renderView
        mMediaPlayer.setPlayerRenderView(renderView)
        if (mLiveContext.roomInfo != null) {
            //已经加入了房间就播放
            mMediaPlayer.start()
        }
    }

    override fun pause() {
        mMediaPlayer.pause()
    }

    override fun resume() {
        mMediaPlayer.resume()
    }

    override fun addPlayerEventListener(playerEventListener: QPlayerEventListener) {
        mMediaPlayer.addEventListener(playerEventListener)
    }

    override fun removePlayerEventListener(playerEventListener: QPlayerEventListener) {
        mMediaPlayer.removeEventListener(playerEventListener)
    }

    override fun getClientType(): QClientType {
        return QClientType.PLAYER
    }

    override var playerGetter: (() -> QIPlayer) = {
        mMediaPlayer
    }

    override fun notifyUserJoin(userId: String) {
        if (mLiveContext.roomInfo?.anchor?.userId?.isEmpty() != false) {
            return
        }
        if (userId == mLiveContext.roomInfo?.anchor?.userId) {
            mLiveContext.checkStatus()
        }
    }

    override fun notifyUserLeft(userId: String) {
        if (mLiveContext.roomInfo?.anchor?.userId?.isEmpty() != false) {
            return
        }
        if (userId == mLiveContext.roomInfo?.anchor?.userId) {
            mLiveContext.checkStatus()
        }
    }

    override fun notifyCheckStatus(newStatus: QLiveStatus, msg: String) {
        mLiveContext.forceSetStatus(newStatus, msg)
    }

    override fun notifyLinkRoleSwitched(isLink: Boolean) {
        mLiveContext.notifyLinkRoleSwitched(isLink)
        mMediaPlayer.switchLinkRole(isLink)
    }
}