package com.qlive.danmakuservice;


import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveService;

import java.util.HashMap;

/**
 * 弹幕服务
 */
public interface QDanmakuService extends QLiveService {

    /**
     * 添加弹幕监听
     * @param listener 弹幕消息监听
     */
    public void addDanmakuServiceListener(QDanmakuServiceListener listener);

    /**
     * 移除弹幕监听
     * @param listener 弹幕消息监听
     */
    public void removeDanmakuServiceListener(QDanmakuServiceListener listener);

    /**
     * 发送弹幕消息
     * @param  msg 弹幕内容
     * @param extension 扩展字段
     * @param callBack  发送回调
     */
    public void sendDanmaku(String msg, HashMap<String,String> extension, QLiveCallBack<QDanmaku> callBack);
}

