package com.qlive.core

/**
 * 直播间状态枚举
 */

fun Int.roomStatusToLiveStatus(): QLiveStatus {
    return when (this) {
        0 -> QLiveStatus.PREPARE
        1 -> QLiveStatus.ON
        2 -> QLiveStatus.FORCE_CLOSE
        else -> QLiveStatus.OFF
    }
}

fun Int.anchorStatusToLiveStatus(): QLiveStatus {
    return when (this) {
        1 -> QLiveStatus.ANCHOR_ONLINE
        0 -> QLiveStatus.ANCHOR_OFFLINE
        else -> QLiveStatus.ANCHOR_OFFLINE
    }
}