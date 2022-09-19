package com.qlive.avparam;

import android.view.View;
/**
 * 推流预览窗口
 * 子类实现 QPushSurfaceView 和 QPushTextureView
 */
public interface QPushRenderView {
    View getView();
}
