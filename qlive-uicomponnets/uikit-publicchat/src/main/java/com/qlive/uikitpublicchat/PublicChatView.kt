package com.qlive.uikitpublicchat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.pubchatservice.QPublicChat
import com.qlive.pubchatservice.QPublicChatService
import com.qlive.pubchatservice.QPublicChatServiceLister
import com.qlive.uikitcore.*
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import java.util.*

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
        var adapterProvider: (context: QLiveUIKitContext, client: QLiveClient) -> QSmartAdapter<QPublicChat> =
            { _, _ ->
                PubChatAdapter()
            }
        internal val msgCache = LinkedList<QPublicChat>()
    }

    private val mAdapter by lazy {
        adapterProvider.invoke(kitContext!!, client!!)
    }

    //消息监听
    private val mPublicChatServiceLister =
        QPublicChatServiceLister {
            msgCache.add(it)
            if (msgCache.size > 10) {
                msgCache.removeFirst()
            }
            mAdapter.addData(it)
            val position = mAdapter.data.size - 1
            this.post {
                this.smoothScrollToPosition(position)
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.layoutManager = LinearLayoutManager(context)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QPublicChatService::class.java)
            .addServiceLister(mPublicChatServiceLister)
        this.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            onItemMsgClickListener.invoke(kitContext!!, client, view, mAdapter.data[position])
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        if (isResumeUIFromFloating) {
            //缓存10跳消息 小窗恢复
            mAdapter.addData(0, msgCache)
            val position = mAdapter.data.size - 1
            if (position > 0) {
                this.post {
                    this.smoothScrollToPosition(position)
                }
            }
        } else {
            msgCache.clear()
        }
    }

    override fun onDestroyed() {
        client?.getService(QPublicChatService::class.java)
            ?.removeServiceLister(mPublicChatServiceLister)
        super.onDestroyed()
    }

    override fun onLeft() {
        super.onLeft()
        msgCache.clear()
        mAdapter.data.clear()
        mAdapter.notifyDataSetChanged()
    }
}