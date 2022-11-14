package com.qlive.avparam;

/**
 * 摄像头参数
 */
public class QCameraParam {

    /**
     * 默认码率
     */
    public static int DEFAULT_BITRATE = 2000;

    /**
     * 分辨率宽 默认值 720
     */
    public int width = 720;
    /**
     * 分辨高  默认值 1280
     */
    public int height = 1280;
    /**
     * 帧率 默认值25
     */
    public int FPS = 25;

    public int bitrate = DEFAULT_BITRATE;

    public QVideoCaptureConfig captureConfig = QVideoCaptureConfigPreset.CAPTURE_1280x720;
}
