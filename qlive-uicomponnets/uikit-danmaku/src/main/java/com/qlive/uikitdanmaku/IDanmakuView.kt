package com.qlive.uikitdanmaku

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qlive.danmakuservice.QDanmaku
import com.qlive.uikitcore.ext.ViewUtil


/**
 * 抽象弹幕UI
 */
interface IDanmakuView {

    var finishedCall: (() -> Unit)?

    /**
     * 是不是同一个轨道上的
     */
    fun showInSameTrack(danmaku: QDanmaku): Boolean

    /**
     * 显示
     */
    fun onNewModel(danmaku: QDanmaku)

    /**
     * 是不是忙碌
     */
    fun isShow(): Boolean

    /**
     * 退出直播间或者切换房间 清空
     * @param isRoomChange 是不是切换直播间
     */
    fun clear(isRoomChange: Boolean = false)
    fun getView(): View
}


interface IDanmuItemView {

    var endCall: (() -> Unit)?

    // 可以开始下一个弹幕了
    var nextAvalibeCall: (() -> Unit)?
    fun clear()
    fun start()
    fun getView(): View
}

/**
 *弹幕条 只有文字
 */
class DanmuItemViewOnlyText : FrameLayout, IDanmuItemView {

    val animatorTime = 6000L
    override var endCall: (() -> Unit)? = null

    // 可以开始下一个弹幕了
    override var nextAvalibeCall: (() -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, mAttributeSet: AttributeSet?) : super(context, mAttributeSet) {
        val view =
            LayoutInflater.from(context).inflate(R.layout.kit_item_danmu_only_text, this, false)
        addView(view)
    }

    private var tansAni: ObjectAnimator? = null
    private fun createAnimal() {
        val transX = ObjectAnimator.ofFloat(
            this,
            "translationX",
            parentWidth.toFloat(),
            -measuredWidth.toFloat()
        )
        transX.interpolator = LinearInterpolator()
        transX.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                endCall?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        transX.addUpdateListener { v: ValueAnimator ->
        }
        tansAni = transX
    }

    private var danmuke: QDanmaku? = null
    private var parentWidth: Int = 0

    fun setDamukeAniml(danmuke: QDanmaku, parentWidth: Int) {
        this.danmuke = danmuke
        this.parentWidth = parentWidth
        translationX = parentWidth.toFloat()
        findViewById<TextView>(R.id.tvContent).text = (danmuke.content)
    }

    override fun clear() {
        nextAvalibeCall = null
        endCall = null
        tansAni?.cancel()
    }

    override fun start() {
        post {
            createAnimal()
            tansAni?.duration = animatorTime
            tansAni?.start()
        }
        postDelayed({
            nextAvalibeCall?.invoke()
        }, (animatorTime * 0.5).toLong())
    }

    override fun getView(): View {
        return this
    }
}

/**
 * 弹幕轨道
 */
class DanmuTrackView : FrameLayout, IDanmakuView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, mAttributeSet: AttributeSet?) : super(context, mAttributeSet) {}

    private var mDanmuItemViews = ArrayList<IDanmuItemView>()
    override var finishedCall: (() -> Unit)? = {}

    override fun showInSameTrack(danmaku: QDanmaku): Boolean {
        return false
    }

    private var nextAble = true;

    override fun onNewModel(danmaku: QDanmaku) {
        val mDanmuItemView = DanmuItemViewOnlyText(context)
        mDanmuItemViews.add(mDanmuItemView)
        addView(
            mDanmuItemView,
            LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        nextAble = false
        mDanmuItemView.nextAvalibeCall = {
            nextAble = true
        }
        mDanmuItemView.endCall = {
            removeView(mDanmuItemView)
            mDanmuItemViews.remove(mDanmuItemView)
        }
        mDanmuItemView.setDamukeAniml(danmaku, getScreenSize())
        mDanmuItemView.start()
    }

    private fun getScreenSize(): Int {
        val displayMetrics: DisplayMetrics =
            Resources.getSystem().displayMetrics
        return intArrayOf(displayMetrics.widthPixels, displayMetrics.heightPixels)[0]
    }

    override fun isShow(): Boolean {
        return !nextAble
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hs = MeasureSpec.makeMeasureSpec(ViewUtil.dip2px(30f), MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, hs)
    }

    override fun clear(isRoomChange: Boolean) {
        mDanmuItemViews.forEach {
            it.clear()
            removeView(it.getView())
        }
        mDanmuItemViews.clear()
    }

    override fun getView(): View {
        return this
    }
}