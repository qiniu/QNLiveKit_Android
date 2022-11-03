package com.qlive.ktvservice

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.qiniu.droid.rtc.QNAudioMusicMixer
import com.qiniu.droid.rtc.QNAudioMusicMixerListener
import com.qiniu.droid.rtc.QNAudioMusicMixerState
import com.qlive.avparam.QIPlayer
import com.qlive.avparam.QPlayerProvider
import com.qlive.avparam.QPlayerSEIListener
import com.qlive.core.QClientType
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QExtension
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.coreimpl.BaseService
import com.qlive.jsonutil.JsonUtils
import com.qlive.ktvservice.QKTVMusic.*
import com.qlive.roomservice.QRoomService
import com.qlive.rtclive.QRTCProvider
import com.qlive.rtclive.QRtcLiveRoom
import com.qlive.rtm.*
import com.qlive.rtm.msg.RtmTextMsg
import java.io.File

class QKTVServiceImpl : QKTVService, BaseService() {

    companion object {
        val key_current_music = "ktv_current_music"
    }

    private var isPendingSwitchTrack = false

    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromID: String, toID: String): Boolean {
            if (toID != currentRoomInfo?.chatID) {
                return false
            }
            if (
                !isLinker && client?.clientType == QClientType.PLAYER
            ) {
                return true
            }
            return parseMsg(msg)
        }
    }

    private val mQPlayerSEIListener = object : QPlayerSEIListener {

        override fun onSEI(sei: String) {
            parseMsg(sei)
        }
    }

    private val audioMixerListener = object : QNAudioMusicMixerListener {
        override fun onStateChanged(p0: QNAudioMusicMixerState) {
            mKTVMusic ?: return
            when (p0) {
                QNAudioMusicMixerState.COMPLETED -> {
                    mKTVMusic!!.playStatus = playStatus_completed
                    saveCurrentPlayingMusicToServer(mKTVMusic!!)
                    sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                    mServiceListenerWrap.onPlayCompleted()
                }

                QNAudioMusicMixerState.PAUSED -> {
                    mKTVMusic!!.playStatus = playStatus_pause
                    saveCurrentPlayingMusicToServer(mKTVMusic!!)
                    sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                    mServiceListenerWrap.onPause()
                }
                QNAudioMusicMixerState.STOPPED -> {
                    if (!isPendingSwitchTrack) {
                        mKTVMusic!!.playStatus = playStatus_stop
                        saveCurrentPlayingMusicToServer(null)
                        sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                        mServiceListenerWrap.onStop()
                        mKTVMusic = null
                    }
                }
                else -> {}
            }
            isPendingSwitchTrack = false
        }

        override fun onMixing(p0: Long) {
            Log.d("QNAudioMixingManager", "onMixing  " + p0)
            if (mKTVMusic != null) {
                mKTVMusic!!.playStatus = playStatus_playing
                mKTVMusic!!.currentPosition = p0
                mKTVMusic!!.currentTimeMillis = System.currentTimeMillis()
                sendKTVMusicSignal(mKTVMusic!!) { isSuccess: Boolean, errorCode: Int, errorMsg: String ->
                }
                mServiceListenerWrap.updatePosition(p0, mKTVMusic!!.duration)
            }
        }

        override fun onError(p0: Int, p1: String) {
            Log.d("QNAudioMixingManager", "onError")
            mKTVMusic?.playStatus = playStatus_error
            saveCurrentPlayingMusicToServer(mKTVMusic!!)
            sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
            mServiceListenerWrap.onError(p0, "rtc mix error")
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo) {
        super.onJoined(roomInfo)
        val musicStr = roomInfo.extension?.get(key_current_music) ?: return
        val music = JsonUtils.parseObject(musicStr, QKTVMusic::class.java) ?: return
        if ((music.playStatus == playStatus_pause || playStatus_playing == music.playStatus) && music.mixerUid != user?.userId) {
            mKTVMusic = music
            mServiceListenerWrap.onStart(music)
        }
        changeMusicAttributes(false)
    }

    override fun attachRoomClient(client: QLiveClient, appContext: Context) {
        super.attachRoomClient(client, appContext)
        RtmManager.addRtmChannelListener(mRtmMsgListener)
        getQPlayerProvider()?.addSEIListener(mQPlayerSEIListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        RtmManager.removeRtmChannelListener(mRtmMsgListener)
        getQPlayerProvider()?.removeSEIListener(mQPlayerSEIListener)
    }

    private fun changeMusicAttributes(isPauseLast: Boolean) {
        mKTVMusic ?: return
        if (mKTVMusic?.mixerUid != user?.userId) {
            when (mKTVMusic!!.playStatus) {
                playStatus_pause -> {
                    mServiceListenerWrap.onPause()
                }
                playStatus_playing -> {
                    if (isPauseLast) {
                        mServiceListenerWrap.onResume()
                    }
                }
                playStatus_error -> {
                    mServiceListenerWrap.onError(1, "主唱混音错误")
                }
                playStatus_stop -> {
                    mServiceListenerWrap.onStop()
                }
            }
            if (mKTVMusic!!.currentPosition >= mKTVMusic!!.duration) {
                mServiceListenerWrap.onPlayCompleted()
            }
            mServiceListenerWrap.updatePosition(
                mKTVMusic!!.currentPosition,//+ System.currentTimeMillis() - mKTVMusic!!.currentTimeMillis,
                mKTVMusic!!.duration
            )
        }
    }

    fun parseMsg(msg: String): Boolean {
        val action = msg.optAction()
        if (action == key_current_music) {
            val dataStr = msg.optData()
            val musicAttribute =
                JsonUtils.parseObject(dataStr, QKTVMusic::class.java) ?: return true
            if (mKTVMusic?.mixerUid == user?.userId) {
                return true
            }
            val isPauseLast = (mKTVMusic?.playStatus ?: -1) == playStatus_pause
            if (mKTVMusic == null
                ||
                mKTVMusic?.musicId != musicAttribute.musicId
            ) {
                mKTVMusic = musicAttribute
                if (mKTVMusic?.mixerUid != user?.userId) {
                    mServiceListenerWrap.onStart(musicAttribute)
                }
            } else {
                if (mKTVMusic?.mixerUid != user?.userId
                    && mKTVMusic?.track != musicAttribute.track
                ) {
                    mServiceListenerWrap.onSwitchTrack(musicAttribute.track)
                }
                mKTVMusic = musicAttribute
            }
            changeMusicAttributes(isPauseLast)
            return true
        }
        return false
    }

    private val mServiceListenerWrap = QKTVServiceListenerWrap()
    private var mQNAudioMixer: QNAudioMusicMixer? = null
    private var mKTVMusic: QKTVMusic? = null

    private fun sendKTVMusicSignal(
        music: QKTVMusic,
        callback: (isSuccess: Boolean, errorCode: Int, errorMsg: String) -> Unit
    ) {
        val rtmMsg = RtmTextMsg(
            key_current_music,
            music
        ).toJsonString()
        RtmManager.rtmClient.sendChannelCMDMsg(
            rtmMsg,
            currentRoomInfo?.chatID ?: "",
            false,
            object : RtmCallBack {
                override fun onSuccess() {
                    callback.invoke(true, 0, "")
                }

                override fun onFailure(code: Int, msg: String) {
                    callback.invoke(false, code, msg)
                }
            })
        getQRTCProvider()?.localVideoTrack?.sendSEI(rtmMsg, 1)
    }

    private fun saveCurrentPlayingMusicToServer(music: QKTVMusic?) {
        client?.getService(QRoomService::class.java)?.updateExtension(QExtension().apply {
            key = key_current_music
            value = if (music != null) {
                JsonUtils.toJson(music)
            } else {
                ""
            }
        }, object : QLiveCallBack<Void> {
            override fun onError(code: Int, msg: String?) {
            }

            override fun onSuccess(data: Void?) {
            }
        })
    }

    override fun play(
        tracks: HashMap<String, String>,
        playTrack: String,
        musicId: String,
        startPosition: Long,
        musicInfo: String
    ): Boolean {
        if (client == null) {
            return false
        }
        if (client?.clientType != QClientType.PUSHER) {
            return false
        }
        if (currentRoomInfo == null) {
            return false
        }
        mQNAudioMixer?.stop()
        val path = tracks[playTrack]
        val duration = QNAudioMusicMixer.getDuration(path)
        var isFirstMIXING = true
        val mQNAudioMixerListener = object : QNAudioMusicMixerListener {
            override fun onStateChanged(p0: QNAudioMusicMixerState) {
                Log.d("QNAudioMixingManager", "onStateChanged  " + p0.name)
                if (p0 == QNAudioMusicMixerState.MIXING) {
                    if (!isFirstMIXING && mKTVMusic!!.playStatus == playStatus_pause) {
                        mKTVMusic!!.playStatus = playStatus_playing
                        saveCurrentPlayingMusicToServer(mKTVMusic!!)
                        sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                        mServiceListenerWrap.onResume()
                    } else {
                        val music = QKTVMusic().apply {
                            this.musicId = musicId
                            mixerUid = user?.userId
                            //开始播放的时间戳
                            startTimeMillis = System.currentTimeMillis()
                            currentPosition = mQNAudioMixer!!.currentPosition
                            currentTimeMillis = System.currentTimeMillis()
                            //播放状态 0 暂停  1 播放  2 出错
                            playStatus = 1
                            this.duration = duration
                            //播放的歌曲信息
                            track = playTrack
                            this.tracks = tracks
                            this.musicInfo = musicInfo
                        }
                        mKTVMusic = music
                        saveCurrentPlayingMusicToServer(music)
                        sendKTVMusicSignal(music) { isSuccess: Boolean, errorCode: Int, errorMsg: String -> }
                        mServiceListenerWrap.onStart(mKTVMusic!!)
                    }
                    isFirstMIXING = false
                }
                audioMixerListener.onStateChanged(p0)
            }

            override fun onMixing(p0: Long) {
                Log.d("QNAudioMixingManager", "onMixing  " + p0)
                audioMixerListener.onMixing(p0)
            }

            override fun onError(p0: Int, p1: String) {
                Log.d("QNAudioMixingManager", "onError ${p0} ${p1}")
                audioMixerListener.onError(p0, p1)
            }
        }

        mQNAudioMixer = getQRTCProvider()!!.localAudioTrack?.createAudioMusicMixer(
            path,
            mQNAudioMixerListener
        )
        mQNAudioMixer?.start(1)

        return mQNAudioMixer == null
    }

    override fun switchTrack(track: String) {
        if (mKTVMusic?.track == track) {
            return
        }
        if (mQNAudioMixer == null) {
            return
        }
        val path = mKTVMusic?.tracks?.get(track) ?: return
        var isFirstMIXING = true
        val orientationPosition = mQNAudioMixer?.currentPosition ?: 0L
        isPendingSwitchTrack = true
        mQNAudioMixer?.stop()

        val mQNAudioMixerListener = object : QNAudioMusicMixerListener {
            override fun onStateChanged(p0: QNAudioMusicMixerState) {
                Log.d("QNAudioMixingManager", "onStateChanged  " + p0.name)
                if (p0 == QNAudioMusicMixerState.MIXING) {
                    if (!isFirstMIXING && mKTVMusic!!.playStatus == playStatus_pause) {
                        mKTVMusic!!.playStatus = playStatus_playing
                        saveCurrentPlayingMusicToServer(mKTVMusic!!)
                        sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                        mServiceListenerWrap.onResume()
                    } else if (isFirstMIXING) {
                        mKTVMusic!!.playStatus = playStatus_playing
                        mKTVMusic!!.track = track
                        saveCurrentPlayingMusicToServer(mKTVMusic!!)
                        sendKTVMusicSignal(mKTVMusic!!) { _, _, _ -> }
                        mServiceListenerWrap.onSwitchTrack(track)
                    }
                    isFirstMIXING = false
                }
                audioMixerListener.onStateChanged(p0)
            }

            override fun onMixing(p0: Long) {
                Log.d("QNAudioMixingManager", "onMixing  " + p0)
                audioMixerListener.onMixing(p0)
            }

            override fun onError(p0: Int, msg: String) {
                Log.d("QNAudioMixingManager", "onError ${p0} ${msg}")
                audioMixerListener.onError(p0, msg)
            }
        }
        mQNAudioMixer = getQRTCProvider()!!.localAudioTrack?.createAudioMusicMixer(
            path,
            mQNAudioMixerListener
        )
        mQNAudioMixer?.startPosition = orientationPosition
        mQNAudioMixer?.start(1)
    }

    override fun seekTo(position: Long) {
        mQNAudioMixer?.seekTo(position)
    }

    override fun pause() {
        mQNAudioMixer?.pause()
    }

    override fun resume() {
        mQNAudioMixer?.resume()
    }

    override fun setMusicVolume(volume: Float) {
        mQNAudioMixer?.mixingVolume = volume
    }

    override fun getMusicVolume(): Float {
        return mQNAudioMixer?.mixingVolume ?: 0f
    }


    override fun getCurrentMusic(): QKTVMusic? {
        return mKTVMusic
    }

    override fun addKTVServiceListener(listener: QKTVServiceListener) {
        mServiceListenerWrap.addListener(listener)
    }

    override fun removeKTVServiceListener(listener: QKTVServiceListener) {
        mServiceListenerWrap.removeListener(listener)
    }

    /**
     * 获得rtc对象
     */

    private fun getQRTCProvider(): QRtcLiveRoom? {
        if (client is QRTCProvider) {
            return (client as QRTCProvider).rtcRoomGetter.invoke()
        }
        return null
    }

    private fun getQPlayerProvider(): QIPlayer? {
        if (client is QPlayerProvider) {
            return (client as QPlayerProvider).playerGetter.invoke()
        }
        return null
    }
}