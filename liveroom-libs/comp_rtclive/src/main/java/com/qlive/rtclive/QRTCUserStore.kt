package com.qlive.rtclive

import com.qiniu.droid.rtc.*
import com.qlive.avparam.QMixStreaming
import java.util.*

internal class QRTCUserStore {
    var meId = ""
    var roomName = ""
    var roomToken = ""
    var localVideoTrack: QNCameraVideoTrack? = null
    var localAudioTrack: QNMicrophoneAudioTrack? = null
    val rtcUsers = LinkedList<QRTCUser>()
    private var localCameraPreview: QNRenderView? = null
    fun addUser(user: QRTCUser) {
        clearUser(user.uid)
        rtcUsers.add(user)
        if (user.uid == meId) {
            user.cameraTrack.preView = localCameraPreview
            user.cameraTrack.track = localVideoTrack
            user.cameraTrack.track = localVideoTrack
        }
    }

    fun clear(destroy: Boolean = true) {
        rtcUsers.clear()
        if (destroy) {
            localAudioTrack?.destroy()
            localVideoTrack?.destroy()
        }
        localAudioTrack = null
        localVideoTrack = null
    }

    fun clearUser(uid: String) {
        findUser(uid)?.let {
            rtcUsers.remove(it)
        }
    }

    fun removeUserTrack(uid: String, track: QNTrack) {
        val user = findUser(uid) ?: return
        when (track) {
            is QNCameraVideoTrack -> {
                user.cameraTrack.track = null
            }
            is QNMicrophoneAudioTrack -> {
                user.microphoneTrack.track = null
            }

            is QNRemoteVideoTrack -> {
                user.cameraTrack.track = null

            }
            is QNRemoteAudioTrack -> {
                user.microphoneTrack.track = null
            }
        }
    }

    fun setUserTrack(uid: String, track: QNTrack) {
        val user = findUser(uid) ?: return
        when (track) {
            is QNCameraVideoTrack -> {
                user.cameraTrack.track = track
                if (!isPlayLocal) {
                    user.cameraTrack.preView?.let {
                        isPlayLocal = true
                        track.play(it)
                    }
                }
            }
            is QNMicrophoneAudioTrack -> {
                user.microphoneTrack.track = track
            }

            is QNRemoteVideoTrack -> {
                user.cameraTrack.track = track
                user.cameraTrack.preView?.let {
                    track.play(it)
                }
            }
            is QNRemoteAudioTrack -> {
                user.microphoneTrack.track = track
            }
        }
    }

    fun setUserCameraPreView(uid: String, preView: QNRenderView) {
        val user = findUser(uid) ?: return
        user.cameraTrack.preView = preView
        user.cameraTrack.track?.tryPlay(preView)
    }

    private var isPlayLocal = false
    fun setLocalCameraPreView(preView: QNRenderView) {
        val user = findUser(meId)
        if (user?.cameraTrack?.track != null) {
            isPlayLocal = true
            user.cameraTrack.preView = preView
            user.cameraTrack.track?.let {
                isPlayLocal = true
                it.tryPlay(preView)
            }
        } else {
            localCameraPreview = preView
            localVideoTrack?.let {
                isPlayLocal = true
                it.play(preView)
            }
        }
    }

    fun findUser(uid: String): QRTCUser? {
        val target: QRTCUser? = rtcUsers.find {
            it.uid == uid
        }
        return target
    }

    fun clearTrackMergeOption() {
        rtcUsers.forEach {
            it.cameraTrack.mTrackMergeOption = null
            it.microphoneTrack.mTrackMergeOption = null
        }
    }

    class QRTCUser {
        var uid: String = ""
        var userExt: Any = ""
        var userData: String = ""
        var cameraTrack = QRTCCameraTrack()
        var microphoneTrack = QRTCMicrophoneTrack()
    }

    class QRTCCameraTrack {
        var track: QNTrack? = null
        var mTrackMergeOption: QMixStreaming.CameraMergeOption? = null
        var preView: QNRenderView? = null
    }

    class QRTCMicrophoneTrack {
        var track: QNTrack? = null
        var mTrackMergeOption: QMixStreaming.MicrophoneMergeOption? = null
    }
}