package com.qlive.rtm

interface RtmMsgListener {
    /**
     * 收到消息
     * @return 是否继续分发
     */
    fun onNewMsg(msg: String, fromID: String, toID: String):Boolean
}