package com.qlive.coreimpl

import com.qlive.core.QLiveStatus

/**
 * Q live service observer
 *
 * @constructor service 向client 通信
 */
interface QLiveServiceObserver {
    fun notifyUserJoin(userId: String)
    fun notifyUserLeft(userId: String)
    fun notifyCheckStatus(newStatus: QLiveStatus, msg: String)
}