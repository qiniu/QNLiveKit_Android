package com.qlive.uikitpublicchat

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.giftservice.QGiftMsg
import com.qlive.giftservice.QGiftService
import com.qlive.giftservice.QGiftServiceListener
import com.qlive.liblog.QLiveLogUtil
import com.qlive.pubchatservice.QPublicChat
import com.qlive.pubchatservice.QPublicChatService
import com.qlive.pubchatservice.QPublicChatServiceLister
import com.qlive.uikitcore.QKitRecyclerView
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.adapter.QRecyclerAdapter
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitcore.tryBackGroundWithLifecycle
import kotlinx.coroutines.delay

//公屏
class PublicChatView : QKitRecyclerView {

    companion object {
        /**
         * 点击事件
         */
        var onItemMsgClickListener: (context: QLiveUIKitContext, client: QLiveClient, view: View, msgMode: QPublicChat) -> Unit =
            { _, _, _, _ -> }

        /**
         * 列表适配器
         */
        var adapterProvider: (context: QLiveUIKitContext, client: QLiveClient) -> QRecyclerAdapter<QPublicChat> =
            { _, _ ->
                PubChatAdapter()
            }
    }

    private val mAdapter by lazy {
        adapterProvider.invoke(kitContext!!, client!!)
    }

    //消息监听
    private val mPublicChatServiceLister =
        QPublicChatServiceLister {
            mAdapter.addData(it)
            val position = mAdapter.data.size - 1
            this.post {
                this.smoothScrollToPosition(position)
            }
        }
    private val mGiftServiceLister = QGiftServiceListener {
        val localMsg = QPublicChat().apply {
            action = QGiftMsg.GIFT_ACTION
            sendUser = it.sender
            content = it.gift.name
            senderRoomId = it.liveID
        }
        client?.getService(QPublicChatService::class.java)?.pubMsgToLocal(localMsg)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.layoutManager = LinearLayoutManager(context)
        initialize(context, attrs)
    }

    private var mMaxHeight = 0
    private fun initialize(context: Context, attrs: AttributeSet?) {
        attrs ?: return
        val arr: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.PublicChatView)
        mMaxHeight = arr.getLayoutDimension(R.styleable.PublicChatView_maxHeight, mMaxHeight)
        arr.recycle()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QPublicChatService::class.java)
            .addServiceLister(mPublicChatServiceLister)
        client.getService(QGiftService::class.java)
            .addGiftServiceListener(mGiftServiceLister)
        mAdapter.setOnItemClickListener { adapter, view, position ->
            onItemMsgClickListener.invoke(kitContext!!, client, view, mAdapter.data[position])
        }
        this.adapter = mAdapter
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        kitContext?.lifecycleOwner?.tryBackGroundWithLifecycle {
            delay(200)
            client?.getService(QPublicChatService::class.java)?.getHistoryChatMsg("", 30,
                object : QLiveCallBack<List<QPublicChat>> {
                    override fun onError(code: Int, msg: String?) {
                        QLiveLogUtil.d(
                            "PublicChatView",
                            " getHistoryChatMsg onError ${code}${msg} "
                        )
                    }

                    override fun onSuccess(data: List<QPublicChat>) {
                        QLiveLogUtil.d(
                            "PublicChatView",
                            " getHistoryChatMsg onSuccess ${data.size} "
                        )
                        mAdapter.addData(0, data.filter {
                            it.action != QPublicChat.action_welcome && it.action != QPublicChat.action_bye
                        })
                        val position = mAdapter.data.size - 1
                        if (position > 0) {
                            this@PublicChatView.post {
                                this@PublicChatView.smoothScrollToPosition(position)
                            }
                        }
                    }
                })
        }
    }

    override fun onDestroyed() {
        client?.getService(QPublicChatService::class.java)
            ?.removeServiceLister(mPublicChatServiceLister)
        client?.getService(QGiftService::class.java)
            ?.removeGiftServiceListener(mGiftServiceLister)
        super.onDestroyed()
    }

    override fun onLeft() {
        super.onLeft()
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (mMaxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}