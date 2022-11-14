package com.qlive.ktvservice;

import com.qlive.core.QLiveService;

import java.util.HashMap;

/**
 * ktv服务
 */
public interface QKTVService extends QLiveService {

    /**
     * 开始播放音乐
     * 仅仅房主能调用
     *
     * @param tracks        音乐轨道 key-轨道名字 value-轨道对映地址
     * @param track         当前选中的轨道
     * @param musicId       音乐ID
     * @param startPosition 开始位置
     * @param musicInfo     歌曲自定义详细信息如：json
     * @return
     */
    public boolean play(HashMap<String, String> tracks,
                        String track,
                        String musicId,
                        long startPosition,
                        String musicInfo
    );

    /**
     * 切换轨道
     * 仅仅房主能调用
     *
     * @param track
     */
    public void switchTrack(String track);

    /**
     * 调整播放位置
     * 仅仅房主能调用
     *
     * @param position
     */
    void seekTo(long position);

    /**
     * 暂停
     * 仅仅房主能调用
     */
    void pause();

    /**
     * 恢复
     * 仅仅房主能调用
     */
    void resume();

    /**
     * 设置音乐音量
     * 仅仅房主能调用
     *
     * @param volume
     */
    void setMusicVolume(float volume);

    /**
     * 获取当前音乐音量
     * 仅仅房主能调用
     *
     * @return
     */
    float getMusicVolume();

    /**
     * 获取当前音乐
     *
     * @return
     */
    QKTVMusic getCurrentMusic();

    /**
     * 音乐监听
     * 房主改变音乐状态
     * 房间所有人通过监听收到状态
     *
     * @param listener
     */
    void addKTVServiceListener(QKTVServiceListener listener);

    void removeKTVServiceListener(QKTVServiceListener listener);
}
