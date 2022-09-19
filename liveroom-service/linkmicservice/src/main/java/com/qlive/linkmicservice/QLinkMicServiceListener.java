package com.qlive.linkmicservice;

import com.qlive.core.been.QExtension;

import org.jetbrains.annotations.NotNull;

/**
 * 麦位监听
 */
public interface QLinkMicServiceListener {

    /**
     * 有人上麦
     *
     * @param micLinker 连麦者
     */
    void onLinkerJoin(QMicLinker micLinker);

    /**
     * 有人下麦
     *
     * @param micLinker 连麦者
     */
    void onLinkerLeft(@NotNull QMicLinker micLinker);

    /**
     * 有人麦克风变化
     *
     * @param micLinker 连麦者
     */
    void onLinkerMicrophoneStatusChange(@NotNull QMicLinker micLinker);

    /**
     * 有人摄像头状态变化
     *
     * @param micLinker 连麦者
     */
    void onLinkerCameraStatusChange(@NotNull QMicLinker micLinker);

    /**
     * 有人被踢
     *
     * @param micLinker 连麦者
     * @param msg 自定义扩展消息
     */
    void onLinkerKicked(@NotNull QMicLinker micLinker, String msg);

    /**
     * 有人扩展字段变化
     *
     * @param micLinker 连麦者
     * @param QExtension 扩展信息
     */
    void onLinkerExtensionUpdate(@NotNull QMicLinker micLinker, QExtension QExtension);
}