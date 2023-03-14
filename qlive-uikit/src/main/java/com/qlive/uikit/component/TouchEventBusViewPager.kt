package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.DecorView
import com.qlive.core.QClientType
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikit.RoomPushActivity
import com.qlive.uikitcore.QLiveComponent
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.ext.isTrailering
import com.qlive.uikitcore.view.CommonViewPagerAdapter

@DecorView
class FrameLayoutBgCover : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }
}

class FrameLayoutSlidingCover : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }
}

/**
 * 通常直播间内多层布局ViewPager会遮挡下层UI触摸事件
 * TouchEventBusViewPager 很好的解决这个事件分发问题，将底层UI放置到ViewPager下级别布局，底层UI能按照原来分发模式进行
 * 同时解决嵌套滑动也很方便
 */
class TouchEventBusViewPager : FrameLayout, QLiveComponent {

    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    private val viewPager by lazy { ViewPager(context) }
    private val views = ArrayList<View>()

    override fun attachKitContext(context: QLiveUIKitContext) {
        super.attachKitContext(context)
        var mFrameLayoutBgCover:FrameLayoutBgCover?=null
        for (i in 0 until childCount) {
            val itemView = getChildAt(i)
            if (itemView is FrameLayoutSlidingCover) {
                views.add(itemView)
            }
            if(itemView is FrameLayoutBgCover){
                mFrameLayoutBgCover=itemView
            }
        }

        views.forEach {
            removeView(it)
        }
        addView(viewPager)
        mFrameLayoutBgCover?.let {
            removeView(it)
            viewPager.addView(it)
        }
        viewPager.adapter = CommonViewPagerAdapter(views)
        viewPager.currentItem = 0

        views.forEach {
            it.visibility = View.INVISIBLE
        }
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        super.onGetLiveRoomInfo(roomInfo)
        val roomId =
            kitContext!!.currentActivity.intent.getStringExtra(RoomPushActivity.KEY_ROOM_ID) ?: ""
        if (roomInfo.isTrailering() && roomId.isNotEmpty()
            && client?.clientType == QClientType.PUSHER
        ) {
            views.forEach {
                it.visibility = View.VISIBLE
            }
            if (views.size > 1) {
                viewPager.setCurrentItem(1, true)
            } else {
                viewPager.currentItem = views.size - 1
            }
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        super.onJoined(roomInfo, isJoinedBefore)
        views.forEach {
            it.visibility = View.VISIBLE
        }
        if (views.size > 1) {
            viewPager.setCurrentItem(1, true)
        } else {
            viewPager.currentItem = views.size - 1
        }
    }

}