package com.qlive.avparam;

import com.qiniu.droid.rtc.QNVideoCaptureConfig;

public class QVideoCaptureConfigPreset {
    public static QVideoCaptureConfig CAPTURE_640x480 = new QVideoCaptureConfig(640, 480, 30);
    public static QVideoCaptureConfig CAPTURE_1280x720 = new QVideoCaptureConfig(1280, 720, 30);
    public static QVideoCaptureConfig CAPTURE_1920x1080 = new QVideoCaptureConfig(1920, 1080, 30);

    public QVideoCaptureConfigPreset() {

    }
}
