package com.qlive.uiwidghtbeauty.model;

import android.graphics.Bitmap;
import android.net.Uri;

public class BeautyItem {

    private int progress;
    private Uri unselectedtIcon;
    private Uri selectedIcon;
    private String text;

    public BeautyItem(String text, Uri unselectedtIcon, Uri selectedtIcon){
        this.text = text;
        this.unselectedtIcon = unselectedtIcon;
        this.selectedIcon = selectedtIcon;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Uri getUnselectedtIcon() {
        return unselectedtIcon;
    }

    public void setUnselectedtIcon(Uri unselectedtIcon) {
        this.unselectedtIcon = unselectedtIcon;
    }

    public Uri getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(Uri selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
