package com.qlive.pushclient;

import com.qlive.avparam.QAudioFrameListener;
import com.qlive.avparam.QBeautySetting;
import com.qlive.avparam.QCameraFace;
import com.qlive.avparam.QCameraParam;
import com.qlive.avparam.QConnectionStatusLister;
import com.qlive.avparam.QMicrophoneParam;
import com.qlive.avparam.QVideoFrameListener;
import com.qlive.core.QClientType;
import com.qlive.core.QLiveCallBack;
import com.qlive.core.QLiveClient;
import com.qlive.core.QLiveService;
import com.qlive.core.QLiveStatusListener;
import com.qlive.core.been.QLiveRoomInfo;
import com.qlive.avparam.QPushRenderView;

/**
 * 推流客户端（主播端）
 */
public interface QPusherClient extends QLiveClient {

    /**
     * 获取插件服务实例
     *
     * @param serviceClass 插件的类
     * @param <T>          QLiveService的子类
     * @return 返回 服务插件对象
     */
    @Override
    <T extends QLiveService> T getService(Class<T> serviceClass);

    /**
     * 设置直播状态回调
     *
     * @param liveStatusListener 直播事件监听
     */
    @Override
    void addLiveStatusListener(QLiveStatusListener liveStatusListener);

    @Override
    void removeLiveStatusListener(QLiveStatusListener liveStatusListener);

    /**
     * 当前客户端类型
     * QClientType.PUSHER 代表推流端
     * QClientType.PLAYER 代表拉流端
     *
     * @return QClientType
     */
    @Override
    QClientType getClientType();


    /**
     * 启动视频采集 和预览
     *
     * @param cameraParam 摄像头参数
     * @param renderView  预览窗口
     */
    void enableCamera(QCameraParam cameraParam, QPushRenderView renderView);

    /**
     * 启动麦克采集
     *
     * @param microphoneParam 麦克风参数
     */
    void enableMicrophone(QMicrophoneParam microphoneParam);

    /**
     * 加入房间
     *
     * @param roomID   房间ID
     * @param callBack 回调函数
     */
    void joinRoom(String roomID, QLiveCallBack<QLiveRoomInfo> callBack);

    /**
     * 主播关闭房间
     *
     * @param callBack
     */
    void closeRoom(QLiveCallBack<Void> callBack);

    /**
     * 主播离开房间 房间不关闭
     *
     * @param callBack
     */
    void leaveRoom(QLiveCallBack<Void> callBack);

    /**
     * 销毁推流客户端
     * 销毁后不能使用
     */
    void destroy();

    /**
     * 主播设置推流链接状态监听
     *
     * @param connectionStatusLister
     */
    void setConnectionStatusLister(QConnectionStatusLister connectionStatusLister);

    /**
     * Switch camera
     *
     * @param callBack 切换摄像头回调
     */
    void switchCamera(QLiveCallBack<QCameraFace> callBack);

    /**
     * 禁/不禁用本地视频流
     * 禁用后本地能看到预览 观众不能看到主播的画面
     *
     * @param muted    是否禁用
     * @param callBack
     */
    void muteCamera(boolean muted, QLiveCallBack<Boolean> callBack);

    /**
     * 当前摄像头状态
     * @return
     */
    boolean isCameraMute();
    /**
     * 禁用麦克风推流
     *
     * @param muted    是否禁用
     * @param callBack
     */
    void muteMicrophone(boolean muted, QLiveCallBack<Boolean> callBack);

    /**
     * 当前摄像头状态
     * @return
     */
    boolean isMicrophoneMute();
    /**
     * 设置视频帧回调
     *
     * @param frameListener 视频帧监听
     */
    void setVideoFrameListener(QVideoFrameListener frameListener);

    /**
     * 设置本地音频数据监听
     *
     * @param frameListener 音频帧回调
     */
    void setAudioFrameListener(QAudioFrameListener frameListener);

    /**
     * 暂停
     */
    void pause();

    /**
     * 恢复
     */
    void resume();

    /**
     * 设置默认免费版美颜参数
     *
     * @param beautySetting 美颜参数
     */
    void setDefaultBeauty(QBeautySetting beautySetting);

    /**
     * 耳返
     * @param isEnable
     */
    void enableEarMonitor(boolean isEnable);
    boolean isEarMonitorEnable();

    /**
     * 音量大小
     * @param volume
     */
    void setMicrophoneVolume(double volume);
    double getMicrophoneVolume();
}
