package com.qlive.uikitlinkmic

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.qlive.core.been.QExtension
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.linkmicservice.QMicLinker
import com.qlive.linkmicservice.QLinkMicServiceListener
import com.qlive.uikitcore.LinkerUIHelper
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitlinkmic.databinding.KitItemLinkerBinding

//麦位预览 多人连麦
class MicSeatPreView : LinearLayout, QLinkMicServiceListener {

    /**
     * 点击事件
     */
    var onItemLinkerClickListener: (view: View, linker: QMicLinker) -> Unit = { _, _ -> }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var isLinker = false
    var linkService: QLinkMicService? = null

    /**
     * 我的角色变跟
     */
    fun setRole(isLinker: Boolean) {
        this.isLinker = isLinker
        for (i in 0 until childCount) {
            (getChildAt(i) as MicItemPreView).setRole(isLinker)
        }
    }

    fun clear() {
        removeAllViews()
    }

    /**
     * 添加一个新的麦位
     */
    override fun onLinkerJoin(micLinker: QMicLinker) {
        val itemView = MicItemPreView(context)
        itemView.linkService = linkService
        itemView.onLinkerJoin(micLinker)
        itemView.setRole(isLinker)
        addView(itemView)
        itemView.setOnClickListener {
            onItemLinkerClickListener.invoke(it, micLinker)
        }
    }

    /**
     * 移除一个麦位
     */
    override fun onLinkerLeft(micLinker: QMicLinker) {
        var target: MicItemPreView? = null
        for (i in 0 until childCount) {
            val item = getChildAt(i) as MicItemPreView
            if (item.mMicLinker?.user?.userId == micLinker.user.userId) {
                target = item
            }
        }
        target?.let {
            it.onLinkerLeft(micLinker)
            this@MicSeatPreView.removeView(it)
        }
    }

    override fun onLinkerMicrophoneStatusChange(micLinker: QMicLinker) {
        var target: MicItemPreView? = null
        for (i in 0 until childCount) {
            val item = getChildAt(i) as MicItemPreView
            if (item.mMicLinker?.user?.userId == micLinker.user.userId) {
                target = item
            }
        }
        target?.onLinkerMicrophoneStatusChange(micLinker)
    }

    override fun onLinkerCameraStatusChange(micLinker: QMicLinker) {
        var target: MicItemPreView? = null
        for (i in 0 until childCount) {
            val item = getChildAt(i) as MicItemPreView
            if (item.mMicLinker?.user?.userId == micLinker.user.userId) {
                target = item
            }
        }
        target?.onLinkerCameraStatusChange(micLinker)
    }

    override fun onLinkerKicked(micLinker: QMicLinker, msg: String?) {

    }

    override fun onLinkerExtensionUpdate(micLinker: QMicLinker, QExtension: QExtension?) {
    }
}

/**
 * 单个麦位item
 */
class MicItemPreView : FrameLayout, QLinkMicServiceListener {

    var mMicLinker: QMicLinker? = null
    private var isLinker = false
    var linkService: QLinkMicService? = null

    /**
     * 角色
     */
    fun setRole(isLinker: Boolean) {
        this.isLinker = isLinker
        if (isLinker) {
            //我上麦了添加预览别人
            addSurface()
        } else {
            //我下麦了切换拉流模式则移除预览 统一拉流播放混流效果
            removeSurface()
        }
    }

    private lateinit var binding: KitItemLinkerBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = KitItemLinkerBinding.inflate(LayoutInflater.from(context), this, true)
        val flVideoMicLp = binding.flVideoMic.layoutParams
        flVideoMicLp.width = LinkerUIHelper.uiMicWidth
        flVideoMicLp.height = LinkerUIHelper.uiMicHeight + LinkerUIHelper.micBottomUIMargin
        binding.flVideoMic.layoutParams = flVideoMicLp

        val tempMixLp = binding.tempMix.layoutParams as FrameLayout.LayoutParams
        tempMixLp.width = LinkerUIHelper.uiMicWidth
        tempMixLp.height = LinkerUIHelper.uiMicHeight + LinkerUIHelper.micBottomUIMargin
        tempMixLp.marginEnd = LinkerUIHelper.micRightUIMargin
        binding.tempMix.layoutParams = tempMixLp
    }

    override fun onLinkerJoin(micLinker: QMicLinker) {
        mMicLinker = micLinker
        if (micLinker.isOpenCamera) {
            binding.flVideoMic.visibility = VISIBLE
            binding.flAudioMic.visibility = View.GONE
        } else {
            binding.flVideoMic.visibility = GONE
            binding.flAudioMic.visibility = View.VISIBLE
        }
        Glide.with(context).load(micLinker.user.avatar)
            .into(binding.ivAvatarInner)
        binding.ivMicStatusInner.isSelected = micLinker.isOpenMicrophone
        binding.ivOutMicStatus.isSelected = micLinker.isOpenMicrophone
    }

    override fun onLinkerLeft(micLinker: QMicLinker) {
        mMicLinker = null
    }

    override fun onLinkerMicrophoneStatusChange(micLinker: QMicLinker) {
        binding.ivMicStatusInner.isSelected = micLinker.isOpenMicrophone
        binding.ivOutMicStatus.isSelected = micLinker.isOpenMicrophone
    }

    override fun onLinkerCameraStatusChange(micLinker: QMicLinker) {
        if (micLinker.isOpenCamera) {
            binding.flVideoMic.visibility = VISIBLE
            binding.flAudioMic.visibility = View.GONE
        } else {
            binding.flVideoMic.visibility = GONE
            binding.flAudioMic.visibility = View.VISIBLE
        }
    }

    override fun onLinkerKicked(micLinker: QMicLinker, msg: String?) {
    }

    override fun onLinkerExtensionUpdate(micLinker: QMicLinker, QExtension: QExtension?) {
    }

    private fun addSurface() {
        val container = binding.flSurfaceContainer as FrameLayout
        val size = ViewUtil.dip2px(96f)
        container.addView(
            RoundTextureView(context).apply {
                linkService?.setUserPreview(mMicLinker?.user?.userId ?: "", this)
                setRadius((size / 2).toFloat())
            },
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )
    }

    private fun removeSurface() {
        val container = binding.flSurfaceContainer as FrameLayout
        container.removeAllViews()
    }
}