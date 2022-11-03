package com.qlive.ktvservice;

public interface QKTVServiceListener {
    //获取房间歌曲信息失败
    void onError(int errorCode, String msg);

    /**
     * 开始播放
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
     * 跟新播放进度
     */
    void updatePosition(long position, long duration);

    /**
     * 播放完成
     */
    void onPlayCompleted();
}
