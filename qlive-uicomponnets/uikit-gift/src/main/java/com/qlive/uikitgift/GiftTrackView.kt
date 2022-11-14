package com.qlive.uikitgift

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.qlive.giftservice.QGiftMsg
import com.qlive.uikitgift.databinding.KitItemGiftTrackBinding

class GiftTrackView : FrameLayout, TrackView {

    private var binding: KitItemGiftTrackBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = KitItemGiftTrackBinding.inflate(LayoutInflater.from(context), this, true)
        alpha = 0f
        translationX = -200f
        translationY = 0f
    }

    @Volatile
    var isShowAnimaing = false

    override var finishedCall: (() -> Unit)? = null

    override fun showInSameTrack(gift: QGiftMsg): Boolean {
        return false
    }

    private val animatorSet // 初始化礼物进场动画
            : AnimatorSet by lazy {

        val inTranslation = ObjectAnimator()
        inTranslation.setPropertyName("translationX")
        inTranslation.setFloatValues(-width.toFloat(), 0f)
        inTranslation.duration = 300
        inTranslation.interpolator = DecelerateInterpolator()

        val inAlpha = ObjectAnimator()
        inAlpha.duration = 300
        inAlpha.setPropertyName("alpha")
        inAlpha.setFloatValues(0f, 1f)

        val outTranslation = ObjectAnimator()
        outTranslation.setPropertyName("translationY")
        outTranslation.setFloatValues(0F, -100f)
        outTranslation.duration = 200
        outTranslation.startDelay = 2000

        val outAlpha = ObjectAnimator()
        outAlpha.duration = 200
        outAlpha.setPropertyName("alpha")
        outAlpha.setFloatValues(1f, 0f)
        outAlpha.startDelay = 2000

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(inTranslation, inAlpha, outAlpha, outTranslation)
        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                isShowAnimaing = false
                this@GiftTrackView.translationX = -width.toFloat()
                this@GiftTrackView.translationY = 0f
            }

            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
        animatorSet
    }

    override fun onNewModel(mode: QGiftMsg) {
        isShowAnimaing = true
        Glide.with(context)
            .load(mode.sender.avatar)
            .into(binding.ivUserAvatar)
        binding.tvGiftName.text =
            String.format(context.getString(R.string.gift_track_send_hit), mode.gift.name)
        binding.tvUserName.text = mode.sender.nick

        when {
            mode.gift.amount in 0..4999 -> {
                binding.ivSmallGiftIcon.visibility = VISIBLE
                binding.ivBigGiftIcon.visibility = INVISIBLE
                binding.llTrack.setBackgroundResource(R.drawable.shape_gift_track_bg_000000)
            }
            mode.gift.amount in 5000..8000 -> {
                binding.ivSmallGiftIcon.visibility = INVISIBLE
                binding.ivBigGiftIcon.visibility = VISIBLE
                binding.llTrack.setBackgroundResource(R.drawable.shape_gift_track_bg_00aae7)
            }
            else -> {
                binding.ivSmallGiftIcon.visibility = INVISIBLE
                binding.ivBigGiftIcon.visibility = VISIBLE
                binding.llTrack.setBackgroundResource(R.drawable.shape_gift_track_bg_ef4149)
            }
        }
        Glide.with(context)
            .load(mode.gift.img)
            .into(binding.ivSmallGiftIcon)
        Glide.with(context)
            .load(mode.gift.img)
            .into(binding.ivBigGiftIcon)

        animatorSet.setTarget(this)
        animatorSet.start()
    }

    override fun isShow(): Boolean {
        return isShowAnimaing
    }

    override fun clear(isRoomChange: Boolean) {

    }

    override fun asView(): View {
        return this
    }
}