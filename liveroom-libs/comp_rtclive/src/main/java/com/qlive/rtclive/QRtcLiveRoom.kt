package com.qlive.rtclive

import android.content.Context
import android.util.Base64
import com.qlive.rtclive.rtc.RtcClientWrap
import com.qlive.rtclive.rtc.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import com.qlive.avparam.QCameraParam
import com.qlive.avparam.QMicrophoneParam
import com.qlive.liblog.QLiveLogUtil
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class QRtcLiveRoom(
    private val appContext: Context,
    private val mQNRTCSetting: QNRTCSetting = QRtcLiveRoomConfig.mRTCSettingGetter.invoke(),
    private val mClientConfig: QNRTCClientConfig = QNRTCClientConfig(
        QNClientMode.LIVE,
        QNClientRole.BROADCASTER
    )
) : RtcClientWrap(appContext, mQNRTCSetting, mClientConfig, true) {
    companion object {
        const val TAG_CAMERA = "camera"

        //屏幕采集轨道的标记
        const val TAG_SCREEN = "screen"
        const val TAG_AUDIO = "audio"
    }

    val localVideoTrack: QNCameraVideoTrack? get() = mQRTCUserStore.localVideoTrack
    val localAudioTrack: QNMicrophoneAudioTrack? get() = mQRTCUserStore.localAudioTrack
    val roomName get() = mQRTCUserStore.roomName
    val roomToken get() = mQRTCUserStore.roomToken
    private var mVideoFrameListener: QNVideoFrameListener? = null
    private var mAudioFrameListener: QNAudioFrameListener? = null
    private var mInnerVideoFrameListener: QNVideoFrameListener? = null

    init {
        QInnerVideoFrameHook.mBeautyHooker?.attach()
        mInnerVideoFrameListener = QInnerVideoFrameHook.mBeautyHooker?.provideVideoFrameListener()
    }

    private fun createVideoTrack(params: QNVideoEncoderConfig): QNCameraVideoTrack {
        // 创建本地 Camera 视频 Track
        val cameraVideoTrackConfig = QNCameraVideoTrackConfig(TAG_CAMERA)
            .apply {
                isMultiProfileEnabled = false
                videoEncoderConfig = params
                videoCaptureConfig = QNVideoCaptureConfigPreset.CAPTURE_1280x720
            }
        return QNRTC.createCameraVideoTrack(cameraVideoTrackConfig);
    }

    private fun createAudioTrack(microphoneAudioTrackConfig: QNMicrophoneAudioTrackConfig): QNMicrophoneAudioTrack {
        return QNRTC.createMicrophoneAudioTrack(QNMicrophoneAudioTrackConfig(TAG_AUDIO).apply {
            audioQuality = microphoneAudioTrackConfig.audioQuality
            isCommunicationModeOn = microphoneAudioTrackConfig.isCommunicationModeOn
        })
    }

    //启动视频采集
    fun enableCamera(cameraParams: QCameraParam) {
        mQRTCUserStore.localVideoTrack = createVideoTrack(
            QNVideoEncoderConfig(
                cameraParams.width,
                cameraParams.height,
                cameraParams.FPS,
                cameraParams.bitrate
            )
        )
        mQRTCUserStore.localVideoTrack?.setVideoFrameListener(
            BeautyVideoFrameListenerWarp(
                mInnerVideoFrameListener,
                mVideoFrameListener
            )
        )
    }

    //启动音频采集
    fun enableMicrophone(microphoneParams: QMicrophoneParam) {
        mQRTCUserStore.localAudioTrack = createAudioTrack(
            QNMicrophoneAudioTrackConfig()
                .setAudioQuality(
                    //QNAudioQualityPreset.STANDARD
                    QNAudioQuality(
                        microphoneParams.sampleRate,
                        microphoneParams.channelCount,
                        microphoneParams.bitsPerSample,
                        microphoneParams.bitrate
                    )
                )
        )
        mQRTCUserStore.localAudioTrack?.setAudioFrameListener { p0, p1, p2, p3, p4 ->
            mAudioFrameListener?.onAudioFrameAvailable(p0, p1, p2, p3, p4)
        }
    }

    suspend fun joinRtc(token: String, msg: String) =
        suspendCoroutine<Unit> { continuation ->
            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)
            val mAppId = json.optString("appId")
            val mRoomName = json.optString("roomName")
            val mUserId = json.optString("userId")
            mQRTCUserStore.meId = mUserId
            mQRTCUserStore.roomName = mRoomName
            mQRTCUserStore.roomToken = token
            val trackQNRTCEngineEvent = object : SimpleQNRTCListener {
                override fun onConnectionStateChanged(
                    state: QNConnectionState,
                    p1: QNConnectionDisconnectedInfo?
                ) {
                    if (state == QNConnectionState.CONNECTED) {
                        removeExtraQNRTCEngineEventListener(this)
                        mQRTCUserStore.addUser(QRTCUserStore.QRTCUser().apply {
                            uid = mUserId
                            userData = msg
                        })
                        continuation.resume(Unit)
                    }

                    if (state == QNConnectionState.DISCONNECTED) {
                        removeExtraQNRTCEngineEventListener(this)
                        continuation.resumeWithException(
                            com.qlive.avparam.RtcException(
                                p1?.errorCode ?: 1,
                                p1?.errorMessage ?: ""
                            )
                        )
                    }
                }
            }
            addExtraQNRTCEngineEventListener(trackQNRTCEngineEvent)
            mClient.join(token, msg)
        }

    suspend fun publishLocal() = suspendCoroutine<Unit> { continuation ->
        val tracks = ArrayList<QNLocalTrack>().apply {
            mQRTCUserStore.localVideoTrack?.let { add(it) }
            mQRTCUserStore.localAudioTrack?.let { add(it) }
        }
        mClient.publish(object : QNPublishResultCallback {
            override fun onPublished() {
                continuation.resume(Unit)
                mQNRTCEngineEventWrap.onLocalPublished(mQRTCUserStore.meId, tracks)
            }

            override fun onError(p0: Int, p1: String) {
                continuation.resumeWithException(com.qlive.avparam.RtcException(p0, p1))
            }
        }, tracks)
    }

    fun leave() {
        mClient.leave()
        mQRTCUserStore.clear()
    }

    fun leaveBgDestroyTrack() {
        mClient.leave()
        var at = mQRTCUserStore.localAudioTrack
        var vt = mQRTCUserStore.localVideoTrack
        Thread {
            QLiveLogUtil.d("leaveBgDestroyTrack", " Track?.destroy()")
            at?.destroy()
            QLiveLogUtil.d("leaveBgDestroyTrack", " localAudioTrack?.destroy()")
            vt?.destroy()
            QLiveLogUtil.d("leaveBgDestroyTrack", " localVideoTrack?.destroy()")
            at = null
            vt = null
        }.start()
        mQRTCUserStore.clear(false)
    }

    fun close() {
        leave()
        mInnerVideoFrameListener = null
        QInnerVideoFrameHook.mBeautyHooker?.detach()
        mAudioFrameListener = null
        mVideoFrameListener = null
        mQNRTCEngineEventWrap.clear()
        QNRTC.deinit()
    }

    //切换摄像头
    fun switchCamera(call: (Boolean?, String) -> Unit) {
        mQRTCUserStore.localVideoTrack?.switchCamera(object : QNCameraSwitchResultCallback {
            override fun onSwitched(p0: Boolean) {
                call.invoke(p0, "")
            }

            override fun onError(p0: String) {
                call.invoke(null, p0)
            }
        })
    }

    //设置本地预览
    fun setLocalPreView(view: QNRenderView) {
        mQRTCUserStore.setLocalCameraPreView(view)
    }

    //禁/不禁 本地摄像头推流
    fun muteLocalCamera(muted: Boolean): Boolean {
        mQRTCUserStore.localVideoTrack?.isMuted = muted
        return mQRTCUserStore.localVideoTrack != null
    }

    //禁/不禁 本地摄像头推流
    fun muteLocalMicrophone(muted: Boolean): Boolean {
        mQRTCUserStore.localAudioTrack?.isMuted = muted
        return mQRTCUserStore.localAudioTrack != null
    }

    //设置视频帧回调
    fun setVideoFrameListener(frameListener: QNVideoFrameListener?) {
        mVideoFrameListener = frameListener
    }

    //设置音频帧回调
    fun setAudioFrameListener(frameListener: QNAudioFrameListener?) {
        mAudioFrameListener = frameListener
    }

    /**
     * 获取某人的视频轨道 如果需要用到track
     */
    fun getUserVideoTrackInfo(uid: String): QNTrack? {
        return mQRTCUserStore.findUser(uid)?.cameraTrack?.track
    }

    /**
     * 获取某人的音频轨道
     */
    fun getUserAudioTrackInfo(uid: String): QNTrack? {
        return mQRTCUserStore.findUser(uid)?.microphoneTrack?.track
    }

    /**
     * 设置某人的摄像头预览窗口 可以在任何时候调用
     */
    fun setUserCameraWindowView(uid: String, view: QNRenderView) {
        mQRTCUserStore.setUserCameraPreView(uid, view)
    }
}