package com.qlive.rtm

import com.qlive.rtm.msg.TextMsg

interface RtmMsgListener {
    /**
     * 收到消息
     * @return 是否继续分发
     */
    fun onNewMsg(msg : TextMsg):Boolean
}