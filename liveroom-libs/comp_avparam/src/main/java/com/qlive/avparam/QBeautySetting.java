package com.qlive.avparam;

/**
 * 默认美颜参数（免费）
 */
public class QBeautySetting {

    private boolean mEnabled = true;
    /**
     * 磨皮
     */
    private float mSmooth;
    /**
     * 美白
     */
    private float mWhiten;
    /**
     * 红润
     */
    private float mRedden;

    public QBeautySetting(float smooth, float whiten, float redden) {
        this.mSmooth = smooth;
        this.mRedden = redden;
        this.mWhiten = whiten;
    }

    public boolean isEnabled() {
        return this.mEnabled;
    }

    /**
     * 设置是否可用
     */
    public void setEnable(boolean enable) {
        this.mEnabled = enable;
    }

    public float getSmoothLevel() {
        return this.mSmooth;
    }

    /**
     * 磨皮等级
     * @param smoothLevel  0.0 -1.0
     */
    public void setSmoothLevel(float smoothLevel) {
        this.mSmooth = smoothLevel;
    }

    public float getWhiten() {
        return this.mWhiten;
    }

    /**
     * 设置美白等级
     * @param whiten  0.0 -1.0
     */
    public void setWhiten(float whiten) {
        this.mWhiten = whiten;
    }

    public float getRedden() {
        return this.mRedden;
    }

    /**
     * 设置红润等级  0.0 -1.0
     * @param redden
     */
    public void setRedden(float redden) {
        this.mRedden = redden;
    }
}
