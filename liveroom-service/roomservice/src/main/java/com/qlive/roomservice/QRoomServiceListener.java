package com.qlive.roomservice;

import com.qlive.core.been.QExtension;

/**
 * 房间服务监听
 */
public interface QRoomServiceListener {
    /**
     * 直播间某个属性变化
     *
     * @param extension 扩展字段
     */
    void onRoomExtensionUpdate(QExtension extension);

    /**
     * 收到管理员审查通知
     *
     * @param message 消息提示
     */
    void onReceivedCensorNotify(String message);

}