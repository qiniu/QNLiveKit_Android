package com.qlive.pkservice;


import com.qlive.core.been.QExtension;

import org.jetbrains.annotations.NotNull;

/**
 * pk回调
 */
public interface QPKServiceListener {

    /**
     * pk开始回调
     * 观众刚进入房间如果房间正在pk也马上会回调
     * @param pkSession pk会话
     */
    void onStart(@NotNull QPKSession pkSession);

    /**
     * pk 结束回调
     * @param pkSession  pk会话
     * @param code -1 异常结束 0主动结束 1对方结束
     * @param msg
     */
    void onStop(@NotNull QPKSession pkSession, int code, @NotNull String msg);

    /**
     * 主播主动开始后 收对方流超时 pk没有建立起来
     * @param pkSession  pk会话
     */
    void onStartTimeOut(@NotNull QPKSession pkSession);

    /**
     * 有pk扩展字段变化
     * @param extension 某个自定义字段
     */
    default void onPKExtensionChange(QExtension extension){};

}