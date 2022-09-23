package com.qlive.coreimpl

import android.content.Context
import com.qlive.core.QClientLifeCycleListener
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveService
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.http.HttpService
import com.qlive.coreimpl.http.NetBzException
import com.qlive.coreimpl.http.PageData
import com.qlive.jsonutil.ParameterizedTypeImpl

open class BaseService : QLiveService, QClientLifeCycleListener {

    protected var user: QLiveUser? = null
    protected var currentRoomInfo: QLiveRoomInfo? = null
    protected var client: QLiveClient? = null

    open fun attachRoomClient(client: QLiveClient, appContext: Context) {
        this.client = client
    }

    /**
     * 进入回调
     *
     * @param user
     */
    override fun onEntering(liveId: String, user: QLiveUser) {
        this.user = user
    }

    /**
     * 加入回调
     *
     * @param roomInfo
     */
    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        this.currentRoomInfo = roomInfo
    }

    /**
     * 离开回调
     */
    override fun onLeft() {
        user = null
    }

    /**
     * 销毁回调
     */
    override fun onDestroyed() {
    }

    open suspend fun checkLeave() {
    }

}