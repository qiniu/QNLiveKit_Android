package com.qlive.ktvservice;

/**
 * 音乐监听
 * 房间里所有人都能监听到当前房间的音乐信息
 */
public interface QKTVServiceListener {

    /**
     * 播放失败
     * @param errorCode
     * @param msg
     */
    void onError(int errorCode, String msg);

    /**
     * 开始播放
     * @param ktvMusic  音乐信息
     */
    void onStart(QKTVMusic ktvMusic);

    /**
     * 切换播放音轨
     */
    void onSwitchTrack(String track);

    /**
     * 暂停
     */
    void onPause();

    /**
     * 恢复
     */
    void onResume();

    void onStop();
    /**
     * 播放进度更新
     * @param position  进度
     * @param duration  文件时长
     */
    void onPositionUpdate(long position, long duration);

    /**
     * 播放完成
     */
    void onPlayCompleted();
}
