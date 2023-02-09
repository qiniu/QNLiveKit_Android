package com.qlive.sdk;

/**
 * qlive sdk 回调
 */
public interface QLiveListener {

    /**
     * im连接状态状态变化
     * @param isConnected 是否链接状态
     */
    void onLoginConnectStatusChanged(boolean isConnected);

    /**
     * 其他设备登录了同一个账号
     * @param deviceSN 设备号
     */
    void onOtherDeviceSingIn(int deviceSN);
}
