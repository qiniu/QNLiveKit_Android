package com.qlive.avparam;

public class QMixStreaming {
    /**
     * 混流画布参数
     */
    public static class MixStreamParams {
        public static int DEFAULT_BITRATE =2000;
        /**
         * 混流画布宽
         */
        public int mixStreamWidth = 0;
        /**
         * 混流画布高
         */
        public int mixStringHeight = 0;
        /**
         * 混流码率
         */
        public int mixBitrate = DEFAULT_BITRATE;
        /**
         * 混流帧率
         */
        public int FPS = 25;
        /**
         * 混流背景图片
         */
        public TranscodingLiveStreamingImage backGroundImg = null;
    }

    /**
     * 背景图片
     */
    public static class TranscodingLiveStreamingImage {
        /**
         * 背景图网络url
         */
        public String url = "";
        /**
         * x坐标
         */
        public int x = 0;
        /**
         * y坐标
         */
        public int y = 0;
        /**
         * 背景图宽
         */
        public int width = 0;
        /**
         * 背景图高
         */
        public int height = 0;
    }

    public static interface TrackMergeOption {

    }

    /**
     * 摄像头混流参数
     */
    public static class CameraMergeOption implements TrackMergeOption {
        /**
         * 是否参与混流
         */
        public boolean isNeed = false;
        /**
         * x坐标
         */
        public int x = 0;
        /**
         * y坐标
         */
        public int y = 0;
        /**
         * z坐标
         */
        public int z = 0;
        /**
         * 用户视频宽
         */
        public int width = 0;
        /**
         * 用户视频高
         */
        public int height = 0;
        // var stretchMode: QNRenderMode? = null

    }

    /**
     * 某个用户的混流参数
     * 只需要指定用户ID 和他的摄像头麦克风混流参数
     */
    public static class MergeOption {
        /**
         * 用户混流参数的ID
         */
        public String uid = "";
        /**
         * 视频混流参数
         */
        public CameraMergeOption cameraMergeOption = new CameraMergeOption();
        /**
         * 音频混流参数
         */
        public MicrophoneMergeOption microphoneMergeOption = new MicrophoneMergeOption();

    }

    /**
     * 麦克风混流参数
     */
    public static class MicrophoneMergeOption implements TrackMergeOption {
        /**
         * 是否参与混流
         */
        public boolean isNeed = false;
    }
}