package com.qlive.uiwidghtbeauty.model;

import static com.qlive.uiwidghtbeauty.utils.Constants.ORIGINAL;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

public class MakeupItem {

    public String name;
    public Uri icon;
    public String path;
    public EffectState state; //0 未下载状态，也是默认状态，1，正在下载状态, 2,下载完毕状态

    public MakeupItem(String name, Uri icon, String path) {
        this.name = name;
        this.icon = icon;
        this.path = path;
        if (TextUtils.isEmpty(this.path)&& !ORIGINAL.equals(name)) {
            state = EffectState.NORMAL_STATE;
        } else {
            state = EffectState.DONE_STATE;
        }
    }
}
