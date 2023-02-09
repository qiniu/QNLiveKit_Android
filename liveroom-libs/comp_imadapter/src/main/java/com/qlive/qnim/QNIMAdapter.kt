package com.qlive.qnim;

import android.content.Context
import android.util.Log
import com.qlive.rtm.RtmCallBack
import com.qlive.rtm.RtmAdapter
import com.qiniu.droid.imsdk.QNIMClient
import com.qlive.liblog.QLiveLogUtil
import com.qlive.rtm.RtmDadaCallBack
import com.qlive.rtm.RtmUserListener
import com.qlive.rtm.msg.TextMsg
import im.floo.BMXCallBack
import im.floo.BMXDataCallBack
import im.floo.floolib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class QNIMAdapter : RtmAdapter {
    val LOG_TAG = "QNIm"
    var isInit = false
        private set

    var isLogin = false
        private set

    private var loginUid = ""
    private var loginImUid = ""
    private var mContext: Context? = null

    private var rtmUserListener: RtmUserListener?=null
    init {
        System.loadLibrary("floo")
    }

    class MsgCallTemp(
        val msg: TextMsg,
        val isDispatchToLocal: Boolean,
        val callBack: RtmCallBack?,
        val isC2c: Boolean,
    )

    private val mMsgCallMap: HashMap<Long, com.qlive.qnim.QNIMAdapter.MsgCallTemp> =
        HashMap<Long, com.qlive.qnim.QNIMAdapter.MsgCallTemp>()
    private var c2cMessageReceiver: (msg: TextMsg) -> Unit =
        { _ -> }
    private var channelMsgReceiver: (msg: TextMsg) -> Unit =
        { _ -> }
    private val mChatListener: BMXChatServiceListener = object : BMXChatServiceListener() {

        override fun onStatusChanged(msg: BMXMessage, error: BMXErrorCode) {
            super.onStatusChanged(msg, error)
            val msgId = msg.clientTimestamp()
            val call: com.qlive.qnim.QNIMAdapter.MsgCallTemp =
                mMsgCallMap.remove(msgId) ?: return

            GlobalScope.launch(Dispatchers.Main) {
                if (error == BMXErrorCode.NoError) {
                    if (call.isDispatchToLocal) {
                        call.msg.msgID = msg.msgId().toString()
                        if (call.isC2c) c2cMessageReceiver(
                            call.msg
                        ) else channelMsgReceiver(
                            call.msg,
                        )
                    }
                    call.callBack?.onSuccess()
                } else {
                    call.callBack?.onFailure(error.swigValue(), error.name)
                }
            }
        }

        override fun onReceiveCommandMessages(list: BMXMessageList) {
            super.onReceiveCommandMessages(list)
            onReceive(list)
        }

        override fun onReceive(list: BMXMessageList) {
            //收到消息
            if (list.isEmpty) {
                return
            }
            for (i in 0 until list.size().toInt()) {
                list[i]?.let { message ->
                    //目标ID
                    val targetId = message.toId().toString()
                    val from = message.fromId().toString()
                    val msgContent = if (message.contentType() == BMXMessage.ContentType.Text
                        || message.contentType() == BMXMessage.ContentType.Command
                    ) {
                        message.content()
                    } else {
                        ""
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        when (message.type()) {
                            BMXMessage.MessageType.Group -> {
                                channelMsgReceiver(
                                    TextMsg(
                                        msgContent,
                                        from,
                                        targetId,
                                        message.msgId().toString()
                                    )
                                )
                            }
                            BMXMessage.MessageType.Single -> {
                                c2cMessageReceiver(
                                    TextMsg(
                                        msgContent,
                                        from,
                                        targetId,
                                        message.msgId().toString()
                                    )
                                )
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
        }
    }
    private val mBMXUserServiceListener = object : BMXUserServiceListener() {
        override fun onConnectStatusChanged(status: BMXConnectStatus) {
            super.onConnectStatusChanged(status)
            rtmUserListener?.onLoginConnectStatusChanged(status==BMXConnectStatus.Connected)
        }

        override fun onOtherDeviceSingIn(deviceSN: Int) {
            super.onOtherDeviceSingIn(deviceSN)
            rtmUserListener?.onOtherDeviceSingIn(deviceSN)
        }
    }

    /**
     * 初始化
     */
    fun init(config: BMXSDKConfig, context: Context) {
        if (QNIMClient.isInit()) {
            QNIMClient.getUserManager()?.removeUserListener(mBMXUserServiceListener)
            QNIMClient.getChatManager()?.removeChatListener(mChatListener)
        }
        QNIMClient.init(config)
        mContext = context
        isInit = true
        QNIMClient.getUserManager().addUserListener(mBMXUserServiceListener)
        QNIMClient.getChatManager().addChatListener(mChatListener)
    }

    fun loginOut(callBack: BMXCallBack) {
        QNIMClient.getUserManager().signOut {
            isLogin = false
            callBack.onResult(it)
        }
    }

    suspend fun loginSuspend(uid: String, loginImUid: String, name: String, pwd: String) =
        suspendCoroutine<BMXErrorCode> { continuation ->
            loginOut {
                loginUid = uid
                this.loginImUid = loginImUid
                QNIMClient.getUserManager().signInByName(name, pwd) { p0 ->
                    if (p0 == BMXErrorCode.NoError) {
                        isLogin = true
                    }
                    continuation.resume(p0)
                }
            }
        }

    //cmd消息暂时没有成功失败回调
    override fun sendC2cCMDMsg(
        msg: String,
        peerId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {
        //目前只处理文本消息
        val targetId = peerId
        val imMsg = BMXMessage.createCommandMessage(
            loginImUid.toLong(),
            peerId.toLong(),
            BMXMessage.MessageType.Single,
            peerId.toLong(),
            (msg)
        )
        QNIMClient.sendMessage(imMsg)
        callBack?.onSuccess()
        if (isDispatchToLocal) {
            c2cMessageReceiver(TextMsg(msg, loginImUid, peerId, imMsg.msgId().toString()))
        }
    }

    // cmd消息暂时没有成功失败回调
    override fun sendChannelCMDMsg(
        msg: String,
        channelId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {
        Log.d("sendChannelMsg", " $channelId  $msg")
        if (channelId.isEmpty()) {
            callBack?.onFailure(0, "")
            return
        }
        val imMsg = BMXMessage.createCommandMessage(
            loginImUid.toLong(),
            channelId.toLong(),
            BMXMessage.MessageType.Group,
            channelId.toLong(),
            (msg)
        )
        QNIMClient.sendMessage(imMsg)
        callBack?.onSuccess()
        if (isDispatchToLocal) {
            channelMsgReceiver(TextMsg(msg, loginImUid, channelId, imMsg.msgId().toString()))
        }
    }

    override fun sendC2cTextMsg(
        msg: String,
        peerId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {
        //目前只处理文本消息
        val targetId = peerId
        val imMsg = BMXMessage.createMessage(
            loginImUid.toLong(),
            peerId.toLong(),
            BMXMessage.MessageType.Single,
            peerId.toLong(),
            (msg)
        )

        val clientTime = System.currentTimeMillis()
        imMsg.setClientTimestamp(clientTime)

        mMsgCallMap.put(
            clientTime,
            com.qlive.qnim.QNIMAdapter.MsgCallTemp(
                TextMsg(msg, loginImUid, peerId, ""),
                isDispatchToLocal,
                callBack,
                false
            )
        )
        QNIMClient.sendMessage(imMsg)
    }

    override fun sendChannelTextMsg(
        msg: String,
        channelId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    ) {
        Log.d("sendChannelMsg", " $channelId  $msg")
        if (channelId.isEmpty()) {
            callBack?.onFailure(0, "")
            return
        }
        val imMsg = BMXMessage.createMessage(
            loginImUid.toLong(),
            channelId.toLong(),
            BMXMessage.MessageType.Group,
            channelId.toLong(),
            (msg)
        )
        val clientTime = System.currentTimeMillis()
        imMsg.setClientTimestamp(clientTime)
        mMsgCallMap.put(
            clientTime,
            com.qlive.qnim.QNIMAdapter.MsgCallTemp(
                TextMsg(msg, loginImUid, channelId, ""),
                isDispatchToLocal,
                callBack,
                false
            )
        )
        QNIMClient.sendMessage(imMsg)
    }

    override fun createChannel(channelId: String, callBack: RtmCallBack?) {
        QNIMClient.getChatRoomManager().create(
            channelId
        ) { p0, p1 ->

            if (p0 == BMXErrorCode.NoError) {
                callBack?.onSuccess()
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }
    }

    override fun joinChannel(channelId: String, callBack: RtmCallBack?) {
        Log.d(LOG_TAG, "joinChannel ${channelId}")
        QNIMClient.getChatRoomManager().join(channelId.toLong()) { p0 ->
            Log.d(LOG_TAG, "joinChannel callback ${p0.name}")
            if (p0 == BMXErrorCode.NoError || p0 == BMXErrorCode.GroupMemberExist) {
                try {
                    callBack?.onSuccess()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }
    }

    override fun leaveChannel(channelId: String, callBack: RtmCallBack?) {
        if (channelId.isEmpty()) {
            callBack?.onFailure(0, "channelId.isEmpty")
            return
        }
        QNIMClient.getChatRoomManager().leave(channelId.toLong()) { p0 ->
            if (p0 == BMXErrorCode.NoError) {
                callBack?.onSuccess()
            } else {
                callBack?.onFailure(p0.swigValue(), p0.name)
            }
        }
    }

    override fun getHistoryTextMsg(
        channelId: String,
        refMsgId: Long,
        size: Int,
        call: RtmDadaCallBack<List<TextMsg>>
    ) {
        QNIMClient.getChatManager().openConversation(
            channelId.toLong(),
            BMXConversation.Type.Group, true
        ) { p0, p1 ->
            if (p0 != BMXErrorCode.NoError || p1 == null) {
                call.onFailure(
                    p0.swigValue(), if (p1 == null) {
                        "Conversation == null"
                    } else {
                        p0.name
                    }
                )
                return@openConversation
            }
            val lastId = if(refMsgId==-1L){
               // p1.lastMsg().msgId()
                refMsgId
            }else{
                refMsgId
            }
            QLiveLogUtil.d("getHistoryTextMsg", "  p1.lastMsg().msgId() $lastId")
            QNIMClient.getChatManager().retrieveHistoryMessages(p1,lastId, size.toLong()) { code, msgList ->
                if (code != BMXErrorCode.NoError) {
                    call.onFailure(code.swigValue(), code.name)
                    return@retrieveHistoryMessages
                }
                val textMsgList = LinkedList<TextMsg>()
                QLiveLogUtil.d("getHistoryTextMsg", " msgList.size()() ${msgList.size()}")
                for (i in 0 until msgList.size()) {
                    val message = msgList.get(i.toInt())
                    val targetId = message.toId().toString()
                    val from = message.fromId().toString()
                    if (message.contentType() == BMXMessage.ContentType.Text
                        || message.contentType() == BMXMessage.ContentType.Command
                    ) {
                        textMsgList.add(
                            TextMsg(message.content(), from, targetId, message.msgId().toString())
                        )
                    }
                }
                call.onSuccess(textMsgList)
            }
        }
    }

    override fun releaseChannel(channelId: String, callBack: RtmCallBack?) {
        //  自动销毁
    }

    override fun getLoginUserId(): String {
        return loginUid
    }

    override fun getLoginUserIMUId(): String {
        return loginImUid
    }

    /**
     * 注册监听
     * @param c2cMessageReceiver  c2c消息接收器
     * @param channelMsgReceiver 群消息接收器
     */
    override fun registerOriginImListener(
        c2cMessageReceiver: (msg: TextMsg) -> Unit,
        channelMsgReceiver: (msg: TextMsg) -> Unit
    ) {
        this.c2cMessageReceiver = c2cMessageReceiver
        this.channelMsgReceiver = channelMsgReceiver
    }

    override fun setRtmUserListener(rtmUserListener: RtmUserListener) {
       this.rtmUserListener=rtmUserListener
    }

}