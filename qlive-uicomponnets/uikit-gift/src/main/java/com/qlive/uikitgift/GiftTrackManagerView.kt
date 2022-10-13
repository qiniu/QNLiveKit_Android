package com.qlive.uikitgift

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.qlive.core.QLiveClient
import com.qlive.uikitcore.QKitLinearLayout
import com.qlive.uikitcore.QLiveUIKitContext

class GiftTrackManagerView : QKitLinearLayout {

    companion object {
        var mGiftTrackSlot = object : GiftTrackSlot {
            override fun createView(
                lifecycleOwner: LifecycleOwner,
                context: QLiveUIKitContext,
                client: QLiveClient,
                container: ViewGroup?
            ): TrackView {
                return GiftTrackView(context.androidContext)
            }

            override fun getTrackCount(): Int {
                return 3
            }
        }
    }

    private lateinit var mGiftTrackManager: GiftTrackManager

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        orientation = VERTICAL
    }

    override fun initView() {
        mGiftTrackManager = GiftTrackManager(client!!)
        mGiftTrackManager.resetView()
        for (i in 0 until mGiftTrackSlot.getTrackCount()) {
            val view = mGiftTrackSlot.createView(
                kitContext!!.lifecycleOwner,
                kitContext!!,
                client!!,
                this
            )
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            addView(view.asView(), lp)
            mGiftTrackManager.addTrackView(view)
        }
    }

    override fun onLeft() {
        super.onLeft()
        mGiftTrackManager.resetView()
    }

    override fun onDestroyed() {
        super.onDestroyed()
        mGiftTrackManager.release()
    }

    interface GiftTrackSlot {
        /**
         * 创建单个轨道
         * @param container
         * @return
         */
        fun createView(
            lifecycleOwner: LifecycleOwner,
            context: QLiveUIKitContext,
            client: QLiveClient,
            container: ViewGroup?
        ): TrackView

        /**
         * 轨道个数
         * @return
         */
        fun getTrackCount(): Int
    }
}