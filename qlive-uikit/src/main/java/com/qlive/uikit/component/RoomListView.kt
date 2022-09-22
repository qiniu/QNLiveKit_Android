package com.qlive.uikit.component

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveStatus
import com.qlive.core.anchorStatusToLiveStatus
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.sdk.QLive
import com.qlive.uikit.R
import com.qlive.uikit.RoomPage
import com.qlive.uikitcore.QComponent
import com.qlive.uikitcore.QUIKitContext
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.bg
import com.qlive.uikitcore.smartrecycler.IAdapter
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import kotlinx.android.synthetic.main.kit_item_room.view.*
import kotlinx.android.synthetic.main.kit_view_room_list.view.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 房间列表view
 */
class RoomListView : FrameLayout, QComponent {
    override var kitContext: QUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_view_room_list, this, true)
        mSmartRecyclerView.recyclerView.layoutManager = GridLayoutManager(context, 2)

        val styled =
            context.obtainStyledAttributes(attrs, R.styleable.RoomListView, defStyleAttr, 0)
        val emptyIcon = styled.getResourceId(
            R.styleable.RoomListView_empty_placeholder_icon,
            com.qlive.uikitcore.R.drawable.kit_pic_empty
        )
        val emptyNoNetIcon = styled.getResourceId(
            R.styleable.RoomListView_empty_placeholder_no_net_icon,
            com.qlive.uikitcore.R.drawable.kit_pic_empty_network
        )
        val emptyTip = styled.getString(R.styleable.RoomListView_empty_placeholder_tips) ?: "空空如也"
        mSmartRecyclerView.emptyView.setEmptyIcon(emptyIcon)
        mSmartRecyclerView.emptyView.setEmptyNoNetIcon(emptyNoNetIcon)
        mSmartRecyclerView.emptyView.setEmptyTips(emptyTip)
        styled.recycle()
        mSmartRecyclerView.recyclerView.addItemDecoration(itemDirection)
    }

    /**
     * 自定义列表适配器
     */
    open var roomAdapter: IAdapter<QLiveRoomInfo> = RoomListAdapter()

    open var itemDirection: RecyclerView.ItemDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            val index = parent.indexOfChild(view)
            val basePadding = 10;
            var left = 0
            var top = 0
            var right = 0
            val bottom = basePadding
            if (index == 0 || index == 1) {
                top = basePadding * 2
            } else {
                top = basePadding
            }
            if (index % 2 == 0) {
                left = basePadding * 2
                right = basePadding
            } else {
                left = basePadding
                right = basePadding * 2
            }
            outRect.top = top
            outRect.bottom = bottom;
            outRect.left = left;
            outRect.right = right;
        }
    }

    override fun attachKitContext(context: QUIKitContext) {
        super.attachKitContext(context)
        mSmartRecyclerView.setUp(
            roomAdapter,
            true,
            true
        ) {
            load(it)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_RESUME) {
            mSmartRecyclerView.startRefresh()
        }
    }

    private suspend fun suspendLoad(page: Int) = suspendCoroutine<List<QLiveRoomInfo>> { ct ->
        QLive.getRooms().listRoom(page + 1, 20, object : QLiveCallBack<List<QLiveRoomInfo>> {
            override fun onError(code: Int, msg: String?) {
                ct.resumeWithException(Exception(msg))
            }

            override fun onSuccess(data: List<QLiveRoomInfo>?) {
                ct.resume(data ?: ArrayList<QLiveRoomInfo>())
            }
        })
    }

    private fun load(page: Int) {
        kitContext?.lifecycleOwner?.bg {
            doWork {
                val list = suspendLoad(page)
                    mSmartRecyclerView.onFetchDataFinish(list, false)
            }
            catchError {
                mSmartRecyclerView.onFetchDataError()
            }
        }
    }

    class RoomListAdapter : QSmartAdapter<QLiveRoomInfo>(
        R.layout.kit_item_room
    ) {

        /**
         * Convert 如何布局每一个房间item
         *
         * @param holder
         * @param item 每一个房间item
         */
        override fun convert(holder: QRecyclerViewHolder, item: QLiveRoomInfo) {
            if (item.anchorStatus.anchorStatusToLiveStatus() != QLiveStatus.ANCHOR_ONLINE) {
                holder.itemView.tvAnchorStatus.visibility = VISIBLE
            } else {
                holder.itemView.tvAnchorStatus.visibility = INVISIBLE
            }
            Glide.with(mContext).load(item.coverURL)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(ViewUtil.dip2px(8f))))
                .into(holder.itemView.ivCover)
            holder.itemView.tvRoomId.text = item.anchor.nick
            holder.itemView.tvRoomName.text = item.title
            holder.itemView.tvOnlineCount.text = item.onlineCount.toString()
            holder.itemView.setOnClickListener {
                QLive.getLiveUIKit().getPage(RoomPage::class.java)
                    .startRoomActivity(mContext, item,
                        object :
                            QLiveCallBack<QLiveRoomInfo> {
                            override fun onError(code: Int, msg: String?) {
                                msg?.asToast(mContext)
                            }

                            override fun onSuccess(data: QLiveRoomInfo?) {
                            }
                        })
            }
        }
    }
}