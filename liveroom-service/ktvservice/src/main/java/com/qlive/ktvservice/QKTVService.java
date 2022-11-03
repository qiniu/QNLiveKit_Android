package com.qlive.ktvservice;

import com.qlive.core.QLiveService;

import java.util.HashMap;

public interface QKTVService extends QLiveService {

    public boolean play(HashMap<String, String> tracks,
                        String track,
                        String musicId,
                        long startPosition,
                        String musicInfo
    );

    public void switchTrack(String track);

    void seekTo(long position);

    void pause();

    void resume();

    void setMusicVolume(float volume);

    float getMusicVolume();

    QKTVMusic getCurrentMusic();

    void addKTVServiceListener(QKTVServiceListener listener);

    void removeKTVServiceListener(QKTVServiceListener listener);
}
