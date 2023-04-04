package com.qlive.coreimpl

import android.content.Context
import com.qlive.core.QClientLifeCycleListener
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveService
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser

/**
 * Base service
 * 业务插件抽象 主要定义插件的规范和能获取到的基础数据
 * @constructor Create empty Base service
 */
open class BaseService : QLiveService ,QClientLifeCycleListener{

    protected var user: QLiveUser? = null
    protected var currentRoomInfo: QLiveRoomInfo? = null
    protected var client: QLiveClient? = null
    protected var isLinker = false

    /**
     * Attach room client
     * 绑定当前client
     * @param client
     * @param appContext
     */
    open fun attachRoomClient(client: QLiveClient, appContext: Context) {
        this.client = client
    }

    /**
     * 进入回调
     *
     * @param user 用户
     * @param liveId 房间
     */
    override fun onEntering(liveId: String, user: QLiveUser) {
        this.user = user
    }

    override fun onJoined(roomInfo: QLiveRoomInfo) {
       currentRoomInfo=roomInfo
    }

    /**
     * 离开之前
     * 如需要提前清理工作
     */
    open suspend fun checkLeave() {
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

    /**
     * 角色变更
     *
     * @param isLink
     */
    open fun onLinkRoleSwitched(isLink: Boolean) {
        isLinker = isLink
    }
}