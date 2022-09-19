package com.qlive.avparam;

import java.nio.ByteBuffer;

/**
 * 音频帧监听
 */
public interface QAudioFrameListener {
    /**
     * 音频帧回调
     *
     * @param srcBuffer        输入pcm数据
     * @param size             大小
     * @param bitsPerSample    位深
     * @param sampleRate       采样率
     * @param numberOfChannels 通道数
     */
    void onAudioFrameAvailable(
            ByteBuffer srcBuffer,
            int size,
            int bitsPerSample,
            int sampleRate,
            int numberOfChannels
    );
}
