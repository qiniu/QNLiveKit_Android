package com.qlive.avparam;

import android.view.Surface;
import android.view.View;

/**
 * 观众播放器预览
 * 子类 QPlayerTextureRenderView 和 QSurfaceRenderView
 */
public interface QPlayerRenderView {
    /**
     * 设置预览模式
     *
     * @param previewMode 预览模式枚举
     */
    void setDisplayAspectRatio(PreviewMode previewMode);

    void setRenderCallback(QRenderCallback rendCallback);

    View getView();

    Surface getSurface();
}
