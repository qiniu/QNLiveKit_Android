package com.qlive.ktvservice;

import java.io.Serializable;
import java.util.HashMap;

public class QKTVMusic implements Serializable {
    public static int playStatus_pause = 0;
    public static int playStatus_playing = 1;
    public static int playStatus_error = 2;
    public static int playStatus_completed = 3;
    public static int playStatus_stop = 4;

    //伴奏
    public static String track_accompany = "accompany";
    //原声
    public static String track_originVoice = "originVoice";

    public static String track_lrc = "lrc";

    //音乐ID
    public String musicId = "";
    //混音主人ID
    public String mixerUid = "";
    //开始播放的时间戳
    public long startTimeMillis = 0;
    //当前进度对应的时间戳
    public long currentTimeMillis = 0;
    //当前播放进度
    public long currentPosition = 0;
    //播放状态 0 暂停  1 播放  2 出错
    public int playStatus = 1;
    //音乐总长度
    public long duration = 0;
    //音轨名称
    public String track = "";
    //播放的歌曲信息
    public String musicInfo = "";

    //轨道信息
    public HashMap<String, String> tracks = new HashMap<String, String>();
}
