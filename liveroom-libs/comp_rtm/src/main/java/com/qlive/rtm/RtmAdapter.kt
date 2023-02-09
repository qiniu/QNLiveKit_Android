package com.qlive.rtm

import com.qlive.rtm.msg.TextMsg

/**
 * im 适配器
 */
interface RtmAdapter {

    /**
     * 发c2c消息
     * @param isDispatchToLocal 发送成功后是否马上往本地的监听分发这个消息
     *
     */
    fun sendC2cCMDMsg(
        msg: String,
        peerId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    )

    /**
     * 发频道消息
     */
    fun sendChannelCMDMsg(
        msg: String,
        channelId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    )


    fun sendC2cTextMsg(
        msg: String,
        peerId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    )

    /**
     * 发频道消息
     */
    fun sendChannelTextMsg(
        msg: String,
        channelId: String,
        isDispatchToLocal: Boolean,
        callBack: RtmCallBack?
    )

    /**
     * 创建频道
     */
    fun createChannel(channelId: String, callBack: RtmCallBack?)

    /**
     * 创建频道
     */
    fun joinChannel(channelId: String, callBack: RtmCallBack?)

    /**
     * 离开频道
     */
    fun leaveChannel(channelId: String, callBack: RtmCallBack?)

    /**
     * 销毁频道
     */
    fun releaseChannel(channelId: String, callBack: RtmCallBack?)

    /**
     * 获得当前登陆用户的id
     */
    fun getLoginUserId(): String

    fun getLoginUserIMUId(): String

    fun getHistoryTextMsg(
        channelId: String,
        refMsgId: Long,
        size: Int,
        call: RtmDadaCallBack<List<TextMsg>>
    )

    /**
     * 注册监听
     * @param c2cMessageReceiver  c2c消息接收器
     * @param channelMsgReceiver 群消息接收器
     */
    fun registerOriginImListener(
        c2cMessageReceiver: (msg: TextMsg) -> Unit,
        channelMsgReceiver: (msg: TextMsg) -> Unit
    )

    fun setRtmUserListener(rtmUserListener: RtmUserListener)
}