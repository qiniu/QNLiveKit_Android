package com.qlive.rtclive

import com.qlive.rtclive.rtc.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import com.qlive.avparam.*
import com.qlive.liblog.QLiveLogUtil
import com.qlive.rtclive.rtc.RtcClientWrap
import kotlinx.coroutines.*

enum class MixType(var isStart: Boolean, var id: String = "") {
    mix(false), forward(false), pk(false)
}

class MixStreamManager(val mQRtcLiveRoom: RtcClientWrap) {
    var mQMixStreamParams: QMixStreaming.MixStreamParams? = null
        private set
    val lastUserMergeOp = HashMap<String, QMixStreaming.MergeOption>()
    private var streamId = ""
    private var pushUrl = ""
    private var serialnum = 1
    private val mEngine by lazy { mQRtcLiveRoom.mClient }
    private var mLastPKStreamParams: QMixStreaming.MixStreamParams? = null
    private var mLastMixStreamParams: QMixStreaming.MixStreamParams? = null

    //混流任务
    var mQNMergeJob: QNTranscodingLiveStreamingConfig? = null
        private set

    //单路转推任务
    var mQNForwardJob: QNDirectLiveStreamingConfig? = null
        private set

    var mPKMergeJob: QNTranscodingLiveStreamingConfig? = null
        private set

    var mMixType = MixType.forward
        private set

    //房间人数
    var roomUser = 0

    var isInit = false

    private var mRestartJob: SchedulerJob? = null
    private fun checkRestartJob() {
        if (mRestartJob != null) {
            return
        }
        mRestartJob = SchedulerJob(2500) {
            if (mMixType.isStart) {
                return@SchedulerJob
            }
            QLiveLogUtil.d(
                "MixStreamHelperImp",
                "MixStreamHelperImp timeOut ${mMixType.name} "
            )
            if (mMixType == MixType.forward) {
                createForwardJob()
                mQNForwardJob?.let {
                    mEngine.startLiveStreaming(it)
                }
                return@SchedulerJob
            }
            if (mMixType == MixType.mix) {
                mQNMergeJob = if (mLastMixStreamParams != null) {
                    createMeop(mLastMixStreamParams!!)
                } else {
                    createDefaultMeOp()
                }
                mQNMergeJob?.let {
                    mEngine.startLiveStreaming(it)
                }
                return@SchedulerJob
            }
            if (mMixType == MixType.pk) {
                mPKMergeJob = if (mLastPKStreamParams != null) {
                    createMeop(mLastPKStreamParams!!)
                } else {
                    createDefaultMeOp()
                }
                mPKMergeJob?.let {
                    mEngine.startLiveStreaming(it)
                }
            }
        }
        mRestartJob?.start()
    }

    fun init(
        streamId: String,
        pushUrl: String,
        QMixStreamParams: QMixStreaming.MixStreamParams
    ) {
        isInit = true
        this.mQMixStreamParams = QMixStreamParams
        this.pushUrl = pushUrl
        this.streamId = streamId
        mEngine.setLiveStreamingListener(object : QNLiveStreamingListener {
            override fun onStarted(streamID: String) {
                QLiveLogUtil.d(
                    "MixStreamHelperImp",
                    "MixStreamHelperImp onStarted ${mMixType.name}"
                )
                // 转推任务创建成功时触发此回调
                mRestartJob?.cancel()
                mRestartJob = null

                mMixType.isStart = true
                if (mMixType == MixType.forward) {
                    stopPKMixStreamJob()
                    stopMixStreamJob()
                }

                if (mMixType == MixType.mix) {
                    stopForwardJob()
                    stopPKMixStreamJob()
                    commitOpt()
                }

                if (mMixType == MixType.pk) {
                    stopForwardJob()
                    stopMixStreamJob()
                    commitOpt()
                }
            }

            override fun onStopped(streamID: String) {
                QLiveLogUtil.d("MixStreamHelperImp", " onStopped  ${streamID}")
                if (mMixType.id != streamID) {
                    return
                }
                mMixType.isStart = false
                // 转推任务成功停止时触发此回调

            }

            override fun onTranscodingTracksUpdated(streamID: String) {
                // 合流布局更新成功时触发此回调
                QLiveLogUtil.d("MixStreamHelperImp", " onTranscodingTracksUpdated  ${streamID}")
            }

            override fun onError(streamID: String, errorInfo: QNLiveStreamingErrorInfo) {
                // 转推任务出错时触发此回调
                QLiveLogUtil.d(
                    "MixStreamHelperImp",
                    "MixStreamHelperImp onError  ${mMixType.name}" + errorInfo.message + "  " + errorInfo.code
                )
                if (mMixType.id != streamID) {
                    return
                }
                mMixType.isStart = false
                mRestartJob?.cancel()
                mRestartJob = null
                checkRestartJob()
            }
        })
        mQRtcLiveRoom.addExtraQNRTCEngineEventListener(object : SimpleQNRTCListener {
            override fun onUserLeft(p0: String) {
                if (mQRtcLiveRoom.mQRTCUserStore.meId == p0) {
                    mQNForwardJob = null
                    mQNMergeJob = null
                    mRestartJob?.cancel()
                }
            }

            override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalPublished(var1, var2)
                val user = mQRtcLiveRoom.mQRTCUserStore.findUser(var1) ?: return
                var2.forEach {
                    if (user.cameraTrack.track?.trackID == it.trackID) {
                        if (user.cameraTrack.mTrackMergeOption?.isNeed == true) {
                            commitOpt()
                        }
                    }
                    if (user.microphoneTrack.track?.trackID == it.trackID) {
                        if (user.microphoneTrack.mTrackMergeOption?.isNeed == true) {
                            commitOpt()
                        }
                    }
                }
            }

            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                val user = mQRtcLiveRoom.mQRTCUserStore.findUser(p0) ?: return
                p1.forEach {
                    if (user.cameraTrack.track?.trackID == it.trackID) {
                        if (user.cameraTrack.mTrackMergeOption?.isNeed == true) {
                            commitOpt()
                        }
                    }
                    if (user.microphoneTrack.track?.trackID == it.trackID) {
                        if (user.microphoneTrack.mTrackMergeOption?.isNeed == true) {
                            commitOpt()
                        }
                    }
                }
            }
        })
    }

    private fun checkUrl(pushUrl: String): String {
        return if (pushUrl.contains("?")) {
            pushUrl + "&serialnum=${serialnum++}"; // 设置合流任务的推流地址
        } else {
            pushUrl + "?serialnum=${serialnum++}"; // 设置合流任务的推流地址
        }
    }

    private fun createDefaultMeOp(): QNTranscodingLiveStreamingConfig {
        return QNTranscodingLiveStreamingConfig().apply {
            streamID = streamId + "?serialnum=${serialnum++}";// 设置 stream id，该 id 为合流任务的唯一标识符
            url = checkUrl(pushUrl)
            QLiveLogUtil.d("MixStreamHelperImp", "createMergeJob${url} ")
            width = mQMixStreamParams!!.mixStreamWidth; // 设置合流画布的宽度
            height = mQMixStreamParams!!.mixStringHeight; // 设置合流画布的高度
            videoFrameRate = mQMixStreamParams!!.FPS; // 设置合流任务的视频帧率
            bitrate = mQMixStreamParams!!.mixBitrate; // 设置合流任务的码率，单位: kbps
        }
    }

    private fun createMeop(mixStreamParams: QMixStreaming.MixStreamParams): QNTranscodingLiveStreamingConfig {
        return QNTranscodingLiveStreamingConfig().apply {
            streamID = streamId + "?serialnum=${serialnum++}";// 设置 stream id，该 id 为合流任务的唯一标识符
            url = checkUrl(pushUrl)
            QLiveLogUtil.d("MixStreamHelperImp", "createMergeJob${url} ")
            width = mixStreamParams.mixStreamWidth; // 设置合流画布的宽度
            height = mixStreamParams.mixStringHeight; // 设置合流画布的高度
            videoFrameRate = mixStreamParams.FPS; // 设置合流任务的视频帧率
//            setRenderMode(QNRenderMode.ASPECT_FILL); // 设置合流任务的默认画面填充方式
            bitrate = mixStreamParams.mixBitrate; // 设置合流任务的码率，单位: kbps
            mixStreamParams.backGroundImg?.let { bg ->
                background = QNTranscodingLiveStreamingImage().apply {
                    this.url = bg.url
                    this.height = bg.height
                    this.width = bg.width
                    this.x = x
                    this.x = y
                }
            }
        }
    }

    //创建前台转推
    private fun createForwardJob() {
        val mDirectLiveStreamingConfig = QNDirectLiveStreamingConfig()
        mDirectLiveStreamingConfig.streamID = streamId + "?serialnum=${serialnum++}"
        mDirectLiveStreamingConfig.url = checkUrl(pushUrl)
        QLiveLogUtil.d("MixStreamHelperImp", "createForwardJob ${mDirectLiveStreamingConfig.url}")
        mQRtcLiveRoom.mQRTCUserStore.localAudioTrack?.let {
            mDirectLiveStreamingConfig.audioTrack = it
        }
        mQRtcLiveRoom.mQRTCUserStore.localVideoTrack?.let {
            mDirectLiveStreamingConfig.videoTrack = it
        }
        mQNForwardJob = mDirectLiveStreamingConfig
        QLiveLogUtil.d("MixStreamHelperImp", "createForwardJob ")
    }

    // private val restartJob =
    /**
     * 启动前台转推 默认实现推本地轨道
     */
    fun startForwardJob() {
        lastUserMergeOp.clear()
        mMixType = MixType.forward
        mMixType.isStart = false
        createForwardJob()
        mMixType.id = mQNForwardJob!!.streamID
        QLiveLogUtil.d("MixStreamHelperImp", "startForwardJob ")
        checkRestartJob()
        mEngine.startLiveStreaming(mQNForwardJob);
    }

    /**
     * 停止前台推流
     */
    private fun stopForwardJob() {
        QLiveLogUtil.d("MixStreamHelperImp", "stopForwardJob ")
        mQNForwardJob?.let {
            mEngine.stopLiveStreaming(mQNForwardJob)
        }
        mQNForwardJob = null
    }

    /**
     * 开始混流转推
     */
    fun startMixStreamJob(mixStreamParams: QMixStreaming.MixStreamParams?) {
        mLastMixStreamParams = mixStreamParams
        mMixType = MixType.mix
        mMixType.isStart = false
        QLiveLogUtil.d("MixStreamHelperImp", "startMixStreamJob ")
        clear()
        if (mixStreamParams != null) {
            mQNMergeJob = createMeop(mixStreamParams)
        } else {
            mQNMergeJob = createDefaultMeOp()
        }
        mMixType.id = mQNMergeJob!!.streamID
        checkRestartJob()
        mEngine.startLiveStreaming(mQNMergeJob)
    }

    private fun clear() {
        QLiveLogUtil.d("MixStreamHelperImp", "clear ")
        mQRtcLiveRoom.mQRTCUserStore.clearTrackMergeOption()
    }

    /**
     * 启动新的混流任务
     */
    fun startPkMixStreamJob(mixStreamParams: QMixStreaming.MixStreamParams?) {
        mLastPKStreamParams = mixStreamParams
        QLiveLogUtil.d("MixStreamHelperImp", "startPkMixStreamJob ")
        clear()
        mMixType = MixType.pk
        mMixType.isStart = false
        if (mixStreamParams != null) {
            mPKMergeJob = createMeop(mixStreamParams)
        } else {
            mPKMergeJob = createDefaultMeOp()
        }
        mMixType.id = mPKMergeJob!!.streamID
        checkRestartJob()
        mEngine.startLiveStreaming(mPKMergeJob);
    }

    private fun stopMixStreamJob() {
        QLiveLogUtil.d("MixStreamHelperImp", "stopMixStreamJob ")

        mQNMergeJob?.let {
            mEngine.stopLiveStreaming(it)
        }
        mQNMergeJob = null
    }

    private fun stopPKMixStreamJob() {
        QLiveLogUtil.d("MixStreamHelperImp", "stopPKMixStreamJob ")
        mPKMergeJob?.let {
            mEngine.stopLiveStreaming(it)
        }
        mPKMergeJob = null
    }

    fun updateUserVideoMergeOptions(
        uid: String,
        option: QMixStreaming.CameraMergeOption?, commitNow: Boolean
    ) {
        val user = mQRtcLiveRoom.mQRTCUserStore.findUser(uid) ?: return
        val cTrack = user.cameraTrack.track
        QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid}")
        user.cameraTrack.mTrackMergeOption = option
        if (cTrack == null) {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid} track没找到")
        } else {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserVideoMergeOptions ${uid} track找到了")
            if (option?.isNeed==true) {
                // tracksMap[trackId] = option
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = cTrack.trackID
                        isSEIEnabled=true
                    })
                )
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    fun updateUserAudioMergeOptions(
        uid: String,
        op: QMixStreaming.MicrophoneMergeOption, commitNow: Boolean
    ) {
        val user = mQRtcLiveRoom.mQRTCUserStore.findUser(uid) ?: return
        val aTrack = user.microphoneTrack.track
        QLiveLogUtil.d("MixStreamHelperImp", "updateUserAudioMergeOptions ${uid}")
        user.microphoneTrack.mTrackMergeOption = op
        if (aTrack == null) {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserAudioMergeOptions ${uid} track没找到")
        } else {
            QLiveLogUtil.d("MixStreamHelperImp", "updateUserAudioMergeOptions ${uid} track找到了")
            if (op.isNeed) {
                // tracksMap[trackId] = op
            } else {
                mEngine.removeTranscodingLiveStreamingTracks(
                    mQNMergeJob!!.streamID,
                    listOf(QNTranscodingLiveStreamingTrack().apply {
                        this.trackID = aTrack.trackID
                        isSEIEnabled=true
                    })
                )
            }
            if (commitNow) {
                commitOpt()
            }
        }
    }

    fun commitOpt() {
        val tracksMap = HashMap<String, QMixStreaming.TrackMergeOption>()
        val toDoAudioMergeOptionsMap = HashMap<String, QMixStreaming.TrackMergeOption>()
        val toDoVideoMergeOptionsMap = HashMap<String, QMixStreaming.TrackMergeOption>()
        mQRtcLiveRoom.mQRTCUserStore.rtcUsers.forEach {
            if (it.cameraTrack.track == null) {
                if (it.cameraTrack.mTrackMergeOption?.isNeed == true) {
                    toDoVideoMergeOptionsMap.put(it.uid, it.cameraTrack.mTrackMergeOption!!)
                }
            } else {
                if (it.cameraTrack.mTrackMergeOption?.isNeed == true) {
                    tracksMap.put(
                        it.cameraTrack.track!!.trackID,
                        it.cameraTrack.mTrackMergeOption!!
                    )
                }
            }

            if (it.microphoneTrack.track == null) {
                if (it.microphoneTrack.mTrackMergeOption?.isNeed == true) {
                    toDoAudioMergeOptionsMap.put(it.uid, it.microphoneTrack.mTrackMergeOption!!)
                }
            } else {
                if (it.microphoneTrack.mTrackMergeOption?.isNeed == true) {
                    tracksMap.put(
                        it.microphoneTrack.track!!.trackID,
                        it.microphoneTrack.mTrackMergeOption!!
                    )
                }
            }
        }

        QLiveLogUtil.d(
            "MixStreamHelperImp",
            "commitOpt fab发布混流参数  mMixType${mMixType.name} ${mMixType.isStart}\n " +
                    "${toDoAudioMergeOptionsMap.size}\n" +
                    "${toDoVideoMergeOptionsMap.size}\n" +
                    "${tracksMap.size}\n" +
                    ""
        )
        if (mMixType != MixType.forward && mMixType.isStart) {
            val mMergeTrackOptions = ArrayList<QNTranscodingLiveStreamingTrack>()
            val sb = StringBuffer("")
            tracksMap.entries.forEach {
                val key = it.key
                val op = it.value
                if (op is QMixStreaming.CameraMergeOption) {
                    val trackOp = QNTranscodingLiveStreamingTrack().apply {
                        trackID = key
                        x = op.x
                        y = op.y
                        zOrder = op.z
                        width = op.width
                        height = op.height
                        isSEIEnabled=true
                        // renderMode = op.stretchMode
                    }
                    mMergeTrackOptions.add(trackOp)
                    sb.append("${key} CameraMergeOption" + trackOp.toJsonObject().toString())
                }
                if (op is QMixStreaming.MicrophoneMergeOption) {
                    val opTrack = QNTranscodingLiveStreamingTrack().apply {
                        trackID = key
                        isSEIEnabled=true
                    }
                    mMergeTrackOptions.add(opTrack)
                    sb.append("${key} MicrophoneMergeOption" + opTrack.toJsonObject().toString())
                }
            }
            QLiveLogUtil.d(
                "MixStreamHelperImp",
                "commitOpt fab发布混流参数  ${sb.toString()}"
            )
            val id = if (mMixType == MixType.mix) {
                mQNMergeJob?.streamID ?: ""
            } else {
                mPKMergeJob?.streamID ?: ""
            }
            mEngine.setTranscodingLiveStreamingTracks(
                id,
                mMergeTrackOptions
            )
        }
    }

    class SchedulerJob(
        private val delayTimeMillis: Long,
        private val coroutineScope: CoroutineScope = GlobalScope,
        val action: suspend CoroutineScope.() -> Unit
    ) {
        private var job: Job? = null
        fun start() {
            job = coroutineScope.launch(Dispatchers.Main) {
                try {
                    while (true) {
                        delay(delayTimeMillis)
                        action()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun cancel() {
            job?.cancel()
            job = null
        }
    }
}