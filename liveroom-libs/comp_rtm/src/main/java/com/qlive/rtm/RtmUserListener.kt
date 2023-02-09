package com.qlive.rtm

interface RtmUserListener {

    /**
     * im连接状态状态变化
     * @param isConnected 是否链接状态
     */
    fun onLoginConnectStatusChanged(isConnected: Boolean)

    /**
     * 其他设备登录了同一个账号
     * @param deviceSN 设备号
     */
    fun onOtherDeviceSingIn(deviceSN: Int)
}