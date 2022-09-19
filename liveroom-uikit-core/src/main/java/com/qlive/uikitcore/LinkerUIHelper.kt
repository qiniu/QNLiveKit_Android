package com.qlive.uikitcore

import com.qlive.avparam.QMixStreaming
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.linkmicservice.QMicLinker
import com.qlive.pkservice.QPKSession

/**
 * 混流参数
 */
object LinkerUIHelper {

    val mixWidth = 720
    val mixHeight = 1280

    /**
     * 混流每个麦位宽大小
     */
    var mixMicWidth = 184

    /**
     * 混流每个麦位高
     */
    var mixMicHeight = 184

    /**
     * 混流第一个麦位上间距
     */
    var mixTopMargin = 174

    /**
     * 混流参数 每个麦位间距
     */
    var micBottomMixMargin = 15

    /**
     * 混流参数 每个麦位右间距
     */
    var micRightMixMargin = 30 * 3


    var uiMicWidth = 0
    var uiMicHeight = 0
    var uiTopMargin = 0
    /**
     * 混流换算成屏幕 每个麦位的间距
     */
    var micBottomUIMargin = 0
    var micRightUIMargin = 0;

    //页面宽高
    var containerWidth = 0
    var containerHeight = 0

    fun attachUIWidth(w: Int, h: Int, centerCrop: Boolean = true) {
        containerWidth = w
        containerHeight = h

        val uiRatio = h.toDouble() / w
        val mixRatio = mixHeight.toDouble() / mixWidth

        var ratio = 0.0
        if (mixRatio > uiRatio) {
            //视频太高
            ratio = containerWidth.toDouble() / mixWidth
            uiMicWidth = (mixMicWidth * ratio).toInt()
            uiMicHeight = (mixMicHeight * ratio).toInt()
            uiTopMargin = (mixTopMargin * ratio - (mixHeight * ratio - containerHeight) / 2).toInt()
            micBottomUIMargin = (micBottomMixMargin * ratio).toInt()
            micRightUIMargin = (micRightMixMargin * ratio).toInt()
        } else {
            //视频太矮
            ratio = containerHeight.toDouble() / mixHeight

            uiMicWidth = (mixMicWidth * ratio).toInt()
            uiMicHeight = (mixMicHeight * ratio).toInt()
            uiTopMargin = (mixTopMargin * ratio).toInt()
            micBottomUIMargin = (micBottomMixMargin * ratio).toInt()
            micRightUIMargin = 0
        }
    }

    /**
     * 获取每个麦位的混流位置参数
     */
    fun getLinkersMixOp(
        micLinkers: List<QMicLinker>,
        roomInfo: QLiveRoomInfo
    ): ArrayList<QMixStreaming.MergeOption> {

        val ops = ArrayList<QMixStreaming.MergeOption>()
        val lastX =
            mixWidth - mixMicWidth - micRightMixMargin
        var lastY = mixTopMargin
        micLinkers.forEach { linker ->

            //主播 0，0 ， 720 ，1280
            if (linker.user.userId == roomInfo.anchor?.userId) {
                ops.add(QMixStreaming.MergeOption().apply {
                    uid = linker.user.userId
                    cameraMergeOption = QMixStreaming.CameraMergeOption().apply {
                        isNeed = true
                        x = 0
                        y = 0
                        z = 0
                        width = mixWidth
                        height = mixHeight
                        // mStretchMode=QNRenderMode.
                    }
                    microphoneMergeOption = QMixStreaming.MicrophoneMergeOption().apply {
                        isNeed = true
                    }
                })
            } else {
                //用户 每个 右上角依次往下排列
                ops.add(QMixStreaming.MergeOption().apply {
                    uid = linker.user.userId
                    cameraMergeOption = QMixStreaming.CameraMergeOption().apply {
                        isNeed = linker.isOpenCamera
                        x = lastX
                        y = lastY
                        z = 1
                        width = mixMicWidth
                        height = mixMicHeight
                        // mStretchMode=QNRenderMode.
                    }
                    lastY += micBottomMixMargin + mixMicHeight
                    microphoneMergeOption = QMixStreaming.MicrophoneMergeOption().apply {
                        isNeed = true
                    }
                })
            }
        }
        return ops
    }

    var isPKing = false
    val pkMixWidth = 720
    val pkMixHeight = 419

    /**
     * PK混流 每个主播位置参数
     */
    fun getPKMixOp(pkSession: QPKSession, user: QLiveUser): ArrayList<QMixStreaming.MergeOption> {
        val ops = ArrayList<QMixStreaming.MergeOption>()
        val peer = if (pkSession.initiator.userId == user.userId) {
            pkSession.receiver
        } else {
            pkSession.initiator
        }
        ops.add(QMixStreaming.MergeOption().apply {
            uid = user.userId
            cameraMergeOption = QMixStreaming.CameraMergeOption().apply {
                isNeed = true
                x = 0
                y = 0
                z = 0
                width = pkMixWidth / 2
                height = pkMixHeight
                // mStretchMode=QNRenderMode.
            }
            microphoneMergeOption = QMixStreaming.MicrophoneMergeOption().apply {
                isNeed = true
            }
        })
        ops.add(QMixStreaming.MergeOption().apply {
            uid = peer.userId
            cameraMergeOption = QMixStreaming.CameraMergeOption().apply {
                isNeed = true
                x = pkMixWidth / 2
                y = 0
                z = 0
                width = pkMixWidth / 2
                height = pkMixHeight
                // mStretchMode=QNRenderMode.
            }
            microphoneMergeOption = QMixStreaming.MicrophoneMergeOption().apply {
                isNeed = true
            }
        })
        return ops
    }

}