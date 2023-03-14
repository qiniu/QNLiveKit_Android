package com.qlive.uikitktv

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.qlive.core.QClientType
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.ktvservice.QKTVMusic
import com.qlive.ktvservice.QKTVService
import com.qlive.ktvservice.QKTVServiceListener
import com.qlive.pushclient.QPusherClient
import com.qlive.uikitcore.QKitViewBindingFrameLayout
import com.qlive.uikitktv.databinding.KitViewKtvCtrolBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

class KTVControlView : QKitViewBindingFrameLayout<KitViewKtvCtrolBinding> {

    private val demoMusic by lazy {
        HashMap<String, String>()
    }
    val rotationYAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.ivMusic, "rotation", 0f, 360f).apply {
            duration = 2000
            repeatCount = -1
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val externalDir = context.getExternalFilesDir(null)
            Utils.doCopy(context.applicationContext, "domemp3", externalDir!!.path)
            demoMusic.put(
                QKTVMusic.track_originVoice,
                externalDir.path + File.separator + "xiaoqingwa.mp3"
            )
            demoMusic.put(
                QKTVMusic.track_accompany,
                externalDir.path + File.separator + "xiaoqingwa_ban.mp3"
            )
            demoMusic.put(
                QKTVMusic.track_lrc,
                externalDir.path + File.separator + "xiaoqingwa.lrc"
            )
        }
    }

    private val mKTVServiceListener = object : QKTVServiceListener {
        override fun onError(errorCode: Int, msg: String?) {
            rotationYAnimator.cancel()
        }

        override fun onStart(ktvMusic: QKTVMusic) {
            binding.tvCurrentSong.text = ktvMusic.musicInfo
            binding.ivPause.isSelected = false
            rotationYAnimator.start()
            showOptionBtn()
        }

        override fun onSwitchTrack(track: String?) {
        }

        override fun onPause() {
            binding.ivPause.isSelected = true
        }

        override fun onResume() {
            binding.ivPause.isSelected = false
            showOptionBtn()
        }

        override fun onStop() {
            binding.ivPause.isSelected = true
            rotationYAnimator.cancel()
            binding.tvCurrentSong.text = "暂无音乐播放"
            hideOptionBtn()
        }

        @SuppressLint("SetTextI18n")
        override fun onPositionUpdate(position: Long, duration: Long) {
            binding.tvPosition.text =
                formatTime(position.toInt()/1000) + "/" + formatTime(duration.toInt()/1000)
        }

        private fun formatTime(time: Int): String {
            val decimalFormat = DecimalFormat("00")
            val hh: String = decimalFormat.format(time / 3600)
            val mm: String = decimalFormat.format(time % 3600 / 60)
            val ss: String = decimalFormat.format(time % 60)
            return if (hh == "00") {
                "$mm:$ss"
            } else {
                "$hh:$mm:$ss"
            }
        }

        override fun onPlayCompleted() {
            rotationYAnimator.cancel()
            binding.ivPause.isSelected = true
            hideOptionBtn()
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QKTVService::class.java).addKTVServiceListener(mKTVServiceListener)
    }

    override fun onDestroyed() {
        super.onDestroyed()
        rotationYAnimator.cancel()
        client?.getService(QKTVService::class.java)?.removeKTVServiceListener(mKTVServiceListener)
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        super.onJoined(roomInfo, isJoinedBefore)
        if (roomInfo.anchor.userId == user?.userId) {
            binding.ivNext.visibility = VISIBLE
        } else {
            binding.ivNext.visibility = GONE
        }
    }

    private fun showOptionBtn() {
        if (roomInfo?.anchor?.userId != user?.userId) {
            return
        }
        binding.ivTrack.visibility = VISIBLE
        binding.ivPause.visibility = VISIBLE
    }

    private fun hideOptionBtn() {
        if (roomInfo?.anchor?.userId != user?.userId) {
            return
        }
        binding.ivTrack.visibility = GONE
        binding.ivPause.visibility = GONE
    }

    override fun initView() {
        binding.ivTrack.visibility = GONE
        binding.ivPause.visibility = GONE
        binding.llMusicInfoContainer.visibility = GONE
        binding.ivTrack.setOnClickListener {
            if (client?.clientType != QClientType.PUSHER) {
                return@setOnClickListener
            }
            client!!.getService(QKTVService::class.java).currentMusic ?: return@setOnClickListener
            MusicSettingDialog(
                client as QPusherClient,
                client!!.getService(QKTVService::class.java)
            ).show(kitContext!!.fragmentManager, "")
        }
        binding.ivNext.setOnClickListener {
            if (client?.clientType != QClientType.PUSHER) {
                return@setOnClickListener
            }
            client!!.getService(QKTVService::class.java).play(
                demoMusic,
                QKTVMusic.track_accompany,
                "1",
                0,
                "小跳蛙"
            )
        }
        binding.ivPause.setOnClickListener {
            if (client?.clientType != QClientType.PUSHER) {
                return@setOnClickListener
            }
            client!!.getService(QKTVService::class.java).currentMusic ?: return@setOnClickListener
            if (it.isSelected) {
                client!!.getService(QKTVService::class.java).resume()
            } else {
                client!!.getService(QKTVService::class.java).pause()
            }
            it.isSelected = !it.isSelected
        }
        binding.tvCurrentSong.text = "暂无音乐播放"

        binding.ivMusic.setOnClickListener {
            if (binding.llMusicInfoContainer.visibility == VISIBLE) {
                binding.llMusicInfoContainer.visibility = GONE
            } else {
                binding.llMusicInfoContainer.visibility = VISIBLE
            }
        }
    }
}