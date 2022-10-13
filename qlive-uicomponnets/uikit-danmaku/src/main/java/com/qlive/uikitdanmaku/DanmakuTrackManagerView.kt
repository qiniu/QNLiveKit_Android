package com.qlive.uikitdanmaku

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.danmakuservice.QDanmakuService
import com.qlive.danmakuservice.QDanmakuServiceListener
import com.qlive.uikitcore.QKitLinearLayout
import com.qlive.uikitcore.QLiveComponent
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.ext.ViewUtil

/**
 * 弹幕轨道管理view
 */
class DanmakuTrackManagerView : QKitLinearLayout {

    companion object {
        var mDanmukeViewSlot: QNDanmukeViewSlot = object : QNDanmukeViewSlot {
            override fun createView(
                lifecycleOwner: LifecycleOwner,
                context: QLiveUIKitContext,
                client: QLiveClient,
                container: ViewGroup?
            ): IDanmakuView {
                return DanmuTrackView(context.androidContext)
            }

            override fun getIDanmakuViewCount(): Int {
                return 3
            }

            override fun topMargin(index: Int): Int {
                return if (index == 0) {
                    ViewUtil.dip2px(124f)
                } else {
                    ViewUtil.dip2px(24f)
                }
            }
        }
    }

    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null
    private val mTrackManager = TrackManager()
    private val mQDanmakuServiceListener =
        QDanmakuServiceListener { danmaku -> mTrackManager.onNewTrackArrive(danmaku) }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        orientation = VERTICAL
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until mDanmukeViewSlot.getIDanmakuViewCount()) {
            val itemView = mDanmukeViewSlot.createView(
                kitContext!!.lifecycleOwner,
                kitContext!!,
                client!!,
                this
            )
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            lp.topMargin = mDanmukeViewSlot.topMargin(i)
            addView(itemView.getView(), lp)
            mTrackManager.addTrackView(itemView)
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QDanmakuService::class.java)
            .addDanmakuServiceListener(mQDanmakuServiceListener)
    }

    override fun initView() {}

    override fun onDestroyed() {
        client?.getService(QDanmakuService::class.java)
            ?.removeDanmakuServiceListener(mQDanmakuServiceListener)
        super.onDestroyed()
    }

    override fun onLeft() {
        super.onLeft()
        mTrackManager.onRoomLeft()
    }

    interface QNDanmukeViewSlot {
        /**
         * 创建单个弹幕轨道
         * @param container
         * @return
         */
        fun createView(
            lifecycleOwner: LifecycleOwner,
            context: QLiveUIKitContext,
            client: QLiveClient,
            container: ViewGroup?
        ): IDanmakuView

        /**
         * 弹幕轨道个数
         * @return
         */
        fun getIDanmakuViewCount(): Int

        /**
         * 距离上一个轨道的上间距
         * @return
         */
        fun topMargin(index: Int): Int
    }
}


