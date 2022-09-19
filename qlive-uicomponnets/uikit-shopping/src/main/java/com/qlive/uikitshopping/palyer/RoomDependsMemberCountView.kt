package com.qlive.uikitshopping.palyer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qlive.chatservice.QChatRoomService
import com.qlive.chatservice.QChatRoomServiceListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.roomservice.QRoomService
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.QRoomComponent
import com.qlive.uikitcore.Scheduler

@SuppressLint("AppCompatCustomView")
class RoomDependsMemberCountView : TextView, QRoomComponent {

    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var client: QLiveClient? = null
    override var kitContext: QLiveUIKitContext? = null

    companion object {
        /**
         * 点击事件回调
         */
        var onClickListener: (context: QLiveUIKitContext?,client:QLiveClient?, view: View) -> Unit =
            { _, _,_ -> }
    }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setOnClickListener {
            onClickListener.invoke(kitContext, client, this)
        }
    }

    private val mChatRoomServiceListener = object :
        QChatRoomServiceListener {
        override fun onUserJoin(memberID: String) {
            refresh(true)
        }

        override fun onUserLeft(memberID: String) {
            refresh(false)
        }

        override fun onUserKicked(memberID: String) {
            refresh(false)
        }
    }


    private fun refresh(add: Boolean?) {

        var count = -1
        try {
            count = (text.toString().toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (count == -1) {
            return
        }
        if (add == true) {
            count++
        }
        if (add == false) {
            count--
        }
        if (count < 1) {
            count = 1
        }
        text = count.toString()
        checkTextSize()
    }

    private fun checkTextSize() {
        if (text.length > 2) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        } else {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
    }

    private val mScheduler = Scheduler(60000) {
        if (roomInfo == null) {
            return@Scheduler
        }
        try {
            val room = client?.getService(QRoomService::class.java)?.getRoomInfo(object :
                QLiveCallBack<QLiveRoomInfo> {
                override fun onError(code: Int, msg: String?) {
                }

                override fun onSuccess(data: QLiveRoomInfo) {
                    text = data.onlineCount.toString()
                    checkTextSize()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        super.onEntering(roomInfo, user)
        mScheduler.cancel()
        mScheduler.start()
        client?.getService(QChatRoomService::class.java)
            ?.addServiceListener(mChatRoomServiceListener)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            mScheduler.cancel()
            client?.getService(QChatRoomService::class.java)
                ?.removeServiceListener(mChatRoomServiceListener)
        }
    }
}