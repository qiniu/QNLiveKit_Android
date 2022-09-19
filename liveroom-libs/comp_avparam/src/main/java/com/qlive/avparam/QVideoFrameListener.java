package com.qlive.avparam;

/**
 * 视频帧监听
 */
public interface QVideoFrameListener {
    /**
     * yuv帧回调
     * @param data yuv数据
     * @param type  帧类型
     * @param width 宽
     * @param height 高
     * @param rotation 旋转角度
     * @param timestampNs 时间戳
     */
    default void onYUVFrameAvailable(
            byte[] data,
            QVideoFrameType type,
            int width,
            int height,
            int rotation,
            long timestampNs
    ) {
    }

    /**
     * 纹理ID回调
     * @param textureID 输入的纹理ID
     * @param type 纹理类型
     * @param width 宽
     * @param height 高
     * @param rotation 旋转角度
     * @param timestampNs 时间戳
     * @param transformMatrix 转化矩阵
     * @return 返回处理后的纹理 如果没有处理 请返回输入的textureID
     */
    default int onTextureFrameAvailable(
            int textureID,
            QVideoFrameType type,
            int width,
            int height,
            int rotation,
            long timestampNs,
            float[] transformMatrix
    ) {
        return textureID;
    }
}
