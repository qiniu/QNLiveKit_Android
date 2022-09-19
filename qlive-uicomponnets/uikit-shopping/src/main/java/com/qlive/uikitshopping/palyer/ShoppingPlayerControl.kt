package com.qlive.uikitshopping.palyer

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.pili.pldroid.player.AVOptions
import com.pili.pldroid.player.PLOnInfoListener
import com.pili.pldroid.player.PLOnVideoSizeChangedListener
import com.pili.pldroid.player.PlayerState
import com.qlive.avparam.PreviewMode
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.liblog.QLiveLogUtil
import com.qlive.shoppingservice.QItem
import com.qlive.uikitcore.*
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitshopping.R
import com.qlive.uikitshopping.WatchExplainingPage.Companion.params_key_item
import kotlinx.android.synthetic.main.kit_view_player_control.view.*
import java.util.*

/**
 * 商品讲解播放器
 * @constructor Create empty Shopping player control
 */
class ShoppingPlayerControl : QRoomComponent, FrameLayout {

    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null
    private val mPlaySpeedSelectDialog by lazy { PlaySpeedSelectDialog() }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_view_player_control, this, true)
        val bufferView =
            LayoutInflater.from(context).inflate(R.layout.kit_player_buffer_view, this, false)
        plVideoView.setBufferingIndicator(bufferView)
        plVideoView.displayAspectRatio = PreviewMode.ASPECT_RATIO_PAVED_PARENT.intValue
        plVideoView.setAVOptions(AVOptions().apply {
            setInteger(AVOptions.KEY_FAST_OPEN, 1);
            setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
            setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO);
            setString(AVOptions.KEY_CACHE_DIR, context.cacheDir.absolutePath)
            setInteger(AVOptions.KEY_LOG_LEVEL, 5)
        })

        error.setDoubleCheckClickListener {
            plVideoView.start()
            checkCurrentStatus()
        }
        center_start.setDoubleCheckClickListener {
            plVideoView.start()
            checkCurrentStatus()
        }
        restart_or_pause.setDoubleCheckClickListener {
            if (plVideoView.isPlaying) {
                plVideoView.pause()
            } else {
                plVideoView.start()
            }
            checkCurrentStatus()
        }
        plVideoView.setOnPreparedListener {
            plVideoView.isLooping = true
        }
        plVideoView.setOnErrorListener { i, any ->
            QLiveLogUtil.d(
                "mIMediaPlayer", "onErrorListener  ${i} ${any}  "
            )
            error.visibility = View.VISIBLE
            center_start.visibility = View.GONE
            true
        }
        plVideoView.setOnVideoSizeChangedListener { w, h ->
            if (w / h.toFloat() < 12 / 16f) {
                plVideoView.displayAspectRatio = (PreviewMode.ASPECT_RATIO_PAVED_PARENT.intValue)
            } else {
                plVideoView.displayAspectRatio = (PreviewMode.ASPECT_RATIO_FIT_PARENT.intValue)
            }
        }
        plVideoView.setOnInfoListener { what, extra, _ ->
            if (what != PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING &&
                what != PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING
            ) {
                checkCurrentStatus()
                QLiveLogUtil.d(
                    "mIMediaPlayer", "PLOnInfoListener  ${what} ${extra}  "
                )
            }
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (plVideoView.playerState == PlayerState.PAUSED) {
                    plVideoView.start()
                }
                val d: Long = plVideoView.duration
                val sp: Long = seekBar.progress.toLong()
                val position = (plVideoView.duration * seekBar.progress / 100f)
                plVideoView.seekTo(position.toLong())
            }
        })
        mPlaySpeedSelectDialog.mDefaultListener =
            object : FinalDialogFragment.BaseDialogListener() {
                override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                    (any as PlaySpeedSelectDialog.PlaySpeed?)?.let {
                        plVideoView.setPlaySpeed(it.speedValue)
                        tvMultiple.text = it.speedName
                    }
                }
            }
        tvMultiple.setOnClickListener {
            mPlaySpeedSelectDialog.show(kitContext!!.fragmentManager, "")
        }
    }


    /**
     * 将毫秒数格式化为"##:##"的时间
     *
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    private fun formatTime(milliseconds: Long): String {
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val totalSeconds = milliseconds / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private val updateProgressJob = Scheduler(1000) {
        if (plVideoView?.playerState == PlayerState.PLAYING) {
            val position: Long = plVideoView!!.currentPosition
            val duration: Long = plVideoView!!.duration
            val bufferPercentage: Int = plVideoView!!.bufferPercentage
            seek.secondaryProgress = bufferPercentage
            val progress = (100f * position / duration).toInt()
            seek.progress = progress
            tvPosition.text = formatTime(position)
            tvDuration.text = formatTime(duration)
        }
    }

    private fun checkCurrentStatus() {
        val status = plVideoView.playerState
        when (status) {
            PlayerState.PLAYING -> {
                error.visibility = View.GONE
                center_start.visibility = View.GONE
                restart_or_pause.setImageResource(R.mipmap.ic_player_pause)
            }

            PlayerState.PAUSED -> {
                error.visibility = View.GONE
                center_start.visibility = View.VISIBLE
                restart_or_pause.setImageResource(R.mipmap.ic_player_start)
            }
        }
        if (plVideoView?.isPlaying == true) {
            if (!updateProgressJob.isStarting) {
                updateProgressJob.start()
            }
        } else {
            updateProgressJob.cancel()
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            updateProgressJob.cancel()
            plVideoView.stopPlayback()
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            plVideoView.pause()
        }
        if (event == Lifecycle.Event.ON_RESUME) {
            if (plVideoView.playerState == PlayerState.PAUSED) {
                plVideoView.start()
                checkCurrentStatus()
            }
        }
    }

    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)
        val item =
            (kitContext?.getIntent()?.getSerializableExtra(params_key_item) as QItem?) ?: return
        plVideoView.setVideoURI(Uri.parse(item.record?.recordURL?:""))
        plVideoView.start()
    }

}