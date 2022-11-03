package com.qlive.linkmicservice;

import com.qlive.avparam.QAudioFrameListener;
import com.qlive.avparam.QBeautySetting;
import com.qlive.avparam.QCameraFace;
import com.qlive.avparam.QCameraParam;
import com.qlive.avparam.QMicrophoneParam;
import com.qlive.avparam.QRoomConnectionState;
import com.qlive.avparam.QVideoFrameListener;
import com.qlive.core.QLiveCallBack;

import java.util.HashMap;

/**
 * 观众连麦器
 */
public interface QAudienceMicHandler {

    /**
     * 观众连麦处理器监听
     * 观众需要处理的事件
     */
    public static interface LinkMicHandlerListener {
        /**
         * 连麦模式连接状态
         * 连接成功后 连麦器会主动禁用推流器 改用rtc
         *
         * @param state 状态
         */
        void onConnectionStateChanged(
                QRoomConnectionState state
        );

        /**
         * 本地角色变化
         *
         * @param isLinker 当前角色是不是麦上用户 上麦后true 下麦后false
         */
        void onRoleChange(boolean isLinker);
    }

    /**
     * 添加连麦监听
     *
     * @param listener 监听
     */
    void addLinkMicListener(LinkMicHandlerListener listener);

    /**
     * 移除连麦监听
     *
     * @param listener 监听
     */
    void removeLinkMicListener(LinkMicHandlerListener listener);

    /**
     * 开始上麦
     *
     * @param extension        麦位扩展字段
     * @param cameraParams     摄像头参数 空代表不开
     * @param microphoneParams 麦克参数  空代表不开
     * @param callBack         上麦成功失败回调
     */
    void startLink(
            HashMap<String, String> extension, QCameraParam cameraParams,
            QMicrophoneParam microphoneParams, QLiveCallBack<Void> callBack
    );

    /**
     * 我是不是麦上用户
     *
     * @return 我是不是麦上用户
     */
    boolean isLinked();

    /**
     * 结束连麦
     *
     * @param callBack 操作回调
     */
    void stopLink(QLiveCallBack<Void> callBack);

    /**
     * 上麦后可以切换摄像头
     *
     * @param callBack
     */
    void switchCamera(QLiveCallBack<QCameraFace> callBack); //切换摄像头

    /**
     * 上麦后可以禁言本地视频流
     *
     * @param muted
     * @param callBack
     */
    void muteCamera(boolean muted, QLiveCallBack<Boolean> callBack); //禁/不禁用本地视频流

    /**
     * 上麦后可以禁用本地音频流
     *
     * @param muted
     * @param callBack
     */
    void muteMicrophone(boolean muted, QLiveCallBack<Boolean> callBack); //禁/不禁用本地麦克风流

    /**
     * 上麦后可以设置本地视频帧回调
     *
     * @param frameListener
     */
    void setVideoFrameListener(QVideoFrameListener frameListener);

    /**
     * 上麦后可以设置音频帧回调
     *
     * @param frameListener
     */
    void setAudioFrameListener(QAudioFrameListener frameListener);

    /**
     * 上麦后可以设置免费的默认美颜参数
     *
     * @param beautySetting
     */
    void setDefaultBeauty(QBeautySetting beautySetting);

    void enableEarMonitor(boolean isEnable);

    boolean isEarMonitorEnable();

    void setMicrophoneVolume(double volume);

    double getMicrophoneVolume();
}
