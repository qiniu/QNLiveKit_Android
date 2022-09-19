package com.qlive.uiwidghtbeauty.ui;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

// 分隔间距,继承于 RecyclerView.ItemDecoration
class BeautyItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public BeautyItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.left = space;
        outRect.right = space;
    }
}