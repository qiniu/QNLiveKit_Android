package com.qlive.uikitlike

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.core.been.QLiveStatistics.TYPE_LIKE_COUNT
import com.qlive.likeservice.QLikeResponse
import com.qlive.likeservice.QLikeService
import com.qlive.sdk.QLive
import com.qlive.uikitcore.QKitViewBindingFrameLayout
import com.qlive.uikitcore.QKitViewBindingFrameMergeLayout
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitlike.databinding.KitLikeViewBinding
import com.qlive.uikitlike.widgets.FaverListener
import com.qlive.uikitlike.widgets.FavorLayout
import java.text.DecimalFormat

class LikeView : QKitViewBindingFrameMergeLayout<KitLikeViewBinding> {

    private var favor: FavorLayout? = null

    private val items by lazy {
        arrayOf<Drawable>(
            ResourcesCompat.getDrawable(
                context.resources,
                R.mipmap.ic_like1,
                context.theme
            )!!,
            ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_like2, context.theme)!!,
            ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_like3, context.theme)!!,
            ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_like4, context.theme)!!,
            ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_like5, context.theme)!!
        )
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setOnClickListener {
            kitContext?.currentActivity ?: return@setOnClickListener
            if (favor == null) {
                favor = FavorLayout(context)
                favor?.faverListener = object : FaverListener {
                    override fun onStart() {}

                    override fun onEnd() {
                        kitContext?.currentActivity!!.findViewById<ViewGroup>(android.R.id.content)
                            .removeView(favor)
                        favor = null
                    }
                }
                kitContext?.currentActivity!!.findViewById<ViewGroup>(android.R.id.content).addView(
                    favor,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                favor?.setFavors(items.toMutableList())
                // 设置效果图标，左右飘动的范围，以及终止点的范围
                favor?.setFavorWidthHeight(100, 400)
                // 设置AncherView，效果图标会从该AncherView的中心点飘出
                favor?.setAncher(this)
            }
            favor?.start(10, 70)

            client?.getService(QLikeService::class.java)
                ?.like(1, object : QLiveCallBack<QLikeResponse> {
                    override fun onError(code: Int, msg: String?) {
                        msg?.asToast(context)
                    }

                    override fun onSuccess(data: QLikeResponse) {
                        setLikeCount(data.total)
                    }
                })
        }
    }

    private fun setLikeCount(total: Int) {
        val totalStr = if (total > 9999) {
            val doubleT = total / 10000.0
            val df = DecimalFormat("#.00")
            df.format(doubleT)
        } else {
            total.toString()
        }
        binding.tvLikeCount.visibility = View.VISIBLE
        binding.tvLikeCount.text = totalStr
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        QLive.getRooms()
            .getLiveStatistics(roomInfo.liveID, object : QLiveCallBack<QLiveStatistics> {
                override fun onError(code: Int, msg: String) {
                    msg.asToast(context)
                }

                override fun onSuccess(data: QLiveStatistics) {
                    var likeInfo: QLiveStatistics.Info? = null
                    data.info.forEach {
                        if (TYPE_LIKE_COUNT == it.type) {
                            likeInfo = it
                        }
                    }

                    if(likeInfo==null){
                        binding.tvLikeCount.visibility = View.INVISIBLE
                    }else{
                        setLikeCount(likeInfo!!.pageView)
                    }
                }
            })
    }

    override fun onLeft() {
        super.onLeft()
        favor?.clear()
    }

    override fun onDestroyed() {
        super.onDestroyed()
        favor?.clear()
    }

    override fun initView() {}
}