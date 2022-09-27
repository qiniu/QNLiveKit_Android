package com.qlive.uiwidghtbeauty.model;

import android.graphics.Bitmap;
import android.net.Uri;

public class StickerOptionsItem {
    public String name;
    public Uri unselectedtIcon;
    public Uri selectedtIcon;

    public StickerOptionsItem(String name, Uri unselectedtIcon, Uri selectedtIcon) {
        this.name = name;
        this.unselectedtIcon = unselectedtIcon;
        this.selectedtIcon = selectedtIcon;
    }
}
