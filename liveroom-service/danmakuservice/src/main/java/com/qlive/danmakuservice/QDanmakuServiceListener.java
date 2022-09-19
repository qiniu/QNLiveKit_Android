package com.qlive.danmakuservice;

/**
 *弹幕消息监听
 */
public interface QDanmakuServiceListener {
    /**
     * 收到弹幕消息
     * @param danmaku 弹幕实体
     */
    void onReceiveDanmaku(QDanmaku danmaku);
}
