package com.qlive.uikitshopping.palyer

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
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
import com.qlive.uikitshopping.databinding.KitViewPlayerControlBinding
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

    private lateinit var binding: KitViewPlayerControlBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = KitViewPlayerControlBinding.inflate(LayoutInflater.from(context), this, true)
        val bufferView =
            LayoutInflater.from(context).inflate(R.layout.kit_player_buffer_view, this, false)
        binding.plVideoView.setBufferingIndicator(bufferView)
        binding.plVideoView.displayAspectRatio = PreviewMode.ASPECT_RATIO_PAVED_PARENT.intValue
        binding.plVideoView.setAVOptions(AVOptions().apply {
            setInteger(AVOptions.KEY_FAST_OPEN, 1);
            setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 5);
            setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO);
            setString(AVOptions.KEY_CACHE_DIR, context.cacheDir.absolutePath)
            setInteger(AVOptions.KEY_LOG_LEVEL, 5)
        })

        binding.error.setDoubleCheckClickListener {
            binding.plVideoView.start()
            checkCurrentStatus()
        }
        binding.root.findViewById<View>(R.id.center_start).setDoubleCheckClickListener {
            binding.plVideoView.start()
            checkCurrentStatus()
        }
        binding.root.findViewById<View>(R.id.restart_or_pause).setDoubleCheckClickListener {
            if (binding.plVideoView.isPlaying) {
                binding.plVideoView.pause()
            } else {
                binding.plVideoView.start()
            }
            checkCurrentStatus()
        }
        binding.plVideoView.setOnPreparedListener {
            binding.plVideoView.isLooping = true
        }
        binding.plVideoView.setOnErrorListener { i, any ->
            QLiveLogUtil.d(
                "mIMediaPlayer", "onErrorListener  ${i} ${any}  "
            )
            binding.error.visibility = View.VISIBLE
            binding.root.findViewById<View>(R.id.center_start).visibility = View.GONE
            true
        }
        binding.plVideoView.setOnVideoSizeChangedListener { w, h ->
            if (w / h.toFloat() < 12 / 16f) {
                binding.plVideoView.displayAspectRatio =
                    (PreviewMode.ASPECT_RATIO_PAVED_PARENT.intValue)
            } else {
                binding.plVideoView.displayAspectRatio =
                    (PreviewMode.ASPECT_RATIO_FIT_PARENT.intValue)
            }
        }
        binding.plVideoView.setOnInfoListener { what, extra, _ ->
            if (what != PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING &&
                what != PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING
            ) {
                checkCurrentStatus()
                QLiveLogUtil.d(
                    "mIMediaPlayer", "PLOnInfoListener  ${what} ${extra}  "
                )
            }
        }
        binding.seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (binding.plVideoView.playerState == PlayerState.PAUSED) {
                    binding.plVideoView.start()
                }
                val d: Long = binding.plVideoView.duration
                val sp: Long = seekBar.progress.toLong()
                val position = (binding.plVideoView.duration * seekBar.progress / 100f)
                binding.plVideoView.seekTo(position.toLong())
            }
        })
        mPlaySpeedSelectDialog.mDefaultListener =
            object : FinalDialogFragment.BaseDialogListener() {
                override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                    (any as PlaySpeedSelectDialog.PlaySpeed?)?.let {
                        binding.plVideoView.setPlaySpeed(it.speedValue)
                        binding.tvMultiple.text = it.speedName
                    }
                }
            }
        binding.tvMultiple.setOnClickListener {
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
        if (binding.plVideoView.playerState == PlayerState.PLAYING) {
            val position: Long = binding.plVideoView!!.currentPosition
            val duration: Long = binding.plVideoView!!.duration
            val bufferPercentage: Int = binding.plVideoView!!.bufferPercentage
            binding.seek.secondaryProgress = bufferPercentage
            val progress = (100f * position / duration).toInt()
            binding.seek.progress = progress
            binding.tvPosition.text = formatTime(position)
            binding.tvDuration.text = formatTime(duration)
        }
    }

    private fun checkCurrentStatus() {
        when (binding.plVideoView.playerState) {
            PlayerState.PLAYING -> {
                binding.error.visibility = View.GONE
                binding.root.findViewById<View>(R.id.center_start).visibility = View.GONE
                binding.root.findViewById<ImageView>(R.id.restart_or_pause)
                    .setImageResource(R.mipmap.ic_player_pause)
            }

            PlayerState.PAUSED -> {
                binding.error.visibility = View.GONE
                binding.root.findViewById<View>(R.id.center_start).visibility = View.VISIBLE
                binding.root.findViewById<ImageView>(R.id.restart_or_pause)
                    .setImageResource(R.mipmap.ic_player_start)
            }
        }
        if (binding.plVideoView.isPlaying) {
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
            binding.plVideoView.stopPlayback()
        }
        if (event == Lifecycle.Event.ON_PAUSE) {
            binding.plVideoView.pause()
        }
        if (event == Lifecycle.Event.ON_RESUME) {
            if (binding.plVideoView.playerState == PlayerState.PAUSED) {
                binding.plVideoView.start()
                checkCurrentStatus()
            }
        }
    }

    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)
        val item =
            (kitContext?.getIntent()?.getSerializableExtra(params_key_item) as QItem?) ?: return
        binding.plVideoView.setVideoURI(Uri.parse(item.record?.recordURL ?: ""))
        binding.plVideoView.start()
    }

}