package com.qlive.uikitcore.ext

import com.qlive.core.QLiveStatus
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.roomStatusToLiveStatus

/**
 * Is trailering
 *判断房间是不是预告中
 * @return
 */
fun QLiveRoomInfo.isTrailering(): Boolean {
    if (this.liveStatus.roomStatusToLiveStatus() == QLiveStatus.PREPARE
    ) {
        return true
    }
    return false
}