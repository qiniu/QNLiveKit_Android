package com.qlive.uiwidghtbeauty.model;

import android.graphics.Bitmap;
import android.net.Uri;

public class FilterItem {

    public String name;
    public Uri icon;
    public String model;

    public FilterItem(String name, Uri icon, String modelName) {
        this.name = name;
        this.icon = icon;
        this.model = modelName;
    }
}
