package com.qlive.uikitlinkmic;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.qlive.rtclive.QPushTextureView;


public class RoundTextureView extends QPushTextureView {
    public RoundTextureView(Context context) {
        super(context);
        init();
    }

    public RoundTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){ }

    public void setRadius(float radius){
        setOutlineProvider(new TextureVideoViewOutlineProvider(radius));
        setClipToOutline(true);
    }

    public static class TextureVideoViewOutlineProvider extends ViewOutlineProvider {
        private  float mRadius;

        public TextureVideoViewOutlineProvider(float radius) {
            this.mRadius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            int leftMargin = 0;
            int topMargin = 0;
            Rect selfRect = new Rect(leftMargin, topMargin,
                    rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
            outline.setRoundRect(selfRect, mRadius);
        }
    }
}
