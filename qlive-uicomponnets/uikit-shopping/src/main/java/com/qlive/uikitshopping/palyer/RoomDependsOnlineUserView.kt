package com.qlive.uikitshopping.palyer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.qlive.chatservice.QChatRoomService
import com.qlive.chatservice.QChatRoomServiceListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.roomservice.QRoomService
import com.qlive.rtm.msg.TextMsg
import com.qlive.uikitcore.*
import com.qlive.uikitcore.adapter.QRecyclerAdapter
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitshopping.R
import com.qlive.uikitshopping.databinding.KitItemOnlineUserWathExpainingBinding
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomDependsOnlineUserView : FrameLayout, QRoomComponent {
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null

    //静态配置
    companion object {
        //单个用户头像点击事件
        var onItemUserClickListener: (context: QLiveUIKitContext?, client: QLiveClient?, view: View, user: QLiveUser) -> Unit =
            { _, _, _, _ -> }

        //列表样式适配器
        var adapterProvider: (context: QLiveUIKitContext, client: QLiveClient) -> QRecyclerAdapter<QLiveUser> =
            { _, _ ->
                OnlineUserViewAdapter()
            }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_shoppingplayer_view_online, this, true)
        findViewById<RecyclerView>(R.id.recyOnline)?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private val adapter: QRecyclerAdapter<QLiveUser> by lazy {
        adapterProvider.invoke(kitContext!!, client!!)
    }

    //聊天室监听
    private val mChatRoomServiceListener = object :
        QChatRoomServiceListener {
        override fun onUserJoin(memberID: String) {
            refresh()
        }

        override fun onUserLeft(memberID: String) {
            refresh()
        }

        override fun onReceivedC2CMsg(msg: TextMsg) {}
        override fun onReceivedGroupMsg(msg: TextMsg) {}
        override fun onUserKicked(memberID: String) {}
        override fun onUserBeMuted(isMute: Boolean, memberID: String, duration: Long) {}
        override fun onAdminAdd(memberID: String) {}
        override fun onAdminRemoved(memberID: String, reason: String) {}
    }

    private var roomId = ""

    private val lazyFreshJob = Scheduler(60000) {
        refresh()
    }

    private suspend fun getOnlineUser() = suspendCoroutine<List<QLiveUser>> { ct ->
        client?.getService(QRoomService::class.java)?.getOnlineUser(1, 10,
            object : QLiveCallBack<List<QLiveUser>> {
                override fun onError(code: Int, msg: String?) {
                    ct.resumeWithException(Exception(msg))
                }

                override fun onSuccess(data: List<QLiveUser>) {
                    ct.resume(data)
                }
            })
    }

    private var isRefreshing = false
    private var lastRefreshedTime = 0L
    private fun refresh() {
        if (isRefreshing) {
            return
        }
        if (System.currentTimeMillis() - lastRefreshedTime < 5 * 1000) {
            return
        }
        if (roomId.isEmpty()) {
            return
        }
        lastRefreshedTime = System.currentTimeMillis()
        kitContext?.lifecycleOwner?.backGround {
            doWork {
                isRefreshing = true
                val users = getOnlineUser().filter {
                    it.userId != roomInfo?.anchor?.userId
                }
                adapter.setNewData(ArrayList(users))
            }
            catchError {
            }
            onFinally {
                isRefreshing = false
            }
        }
    }

    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)
        roomId = roomInfo.liveID
        lazyFreshJob.cancel()
        lazyFreshJob.start()
    }

    override fun attachLiveClient(client: QLiveClientClone) {
        super.attachLiveClient(client)
        adapter.setOnItemClickListener { _, view, position ->
            onItemUserClickListener.invoke(
                kitContext,
                client,
                view,
                adapter.data[position]
            )
        }
        findViewById<RecyclerView>(R.id.recyOnline).adapter = adapter
        client.getService(QChatRoomService::class.java)
            .addServiceListener(mChatRoomServiceListener)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            lazyFreshJob.cancel()
            client?.getService(QChatRoomService::class.java)
                ?.removeServiceListener(mChatRoomServiceListener)
        }
    }

    class OnlineUserViewAdapter :
        QSmartViewBindAdapter<QLiveUser, KitItemOnlineUserWathExpainingBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemOnlineUserWathExpainingBinding>,
            item: QLiveUser
        ) {
            Glide.with(mContext)
                .load(item.avatar)
                .into(helper.binding.ivAvatar)
        }
    }
}