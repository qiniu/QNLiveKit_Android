package com.qlive.uikitcore.refresh;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class MaterialProgressDrawable extends Drawable implements Animatable {
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final float FULL_ROTATION = 1080.0F;
    public static final byte LARGE = 0;
    public static final byte DEFAULT = 1;
    private static final byte CIRCLE_DIAMETER = 40;
    private static final float CENTER_RADIUS = 8.75F;
    private static final float STROKE_WIDTH = 2.5F;
    private static final byte CIRCLE_DIAMETER_LARGE = 56;
    private static final float CENTER_RADIUS_LARGE = 12.5F;
    private static final float STROKE_WIDTH_LARGE = 3.0F;
    private static final int[] COLORS = new int[]{-16777216};
    private static final float COLOR_START_DELAY_OFFSET = 0.75F;
    private static final float END_TRIM_START_DELAY_OFFSET = 0.5F;
    private static final float START_TRIM_DURATION_OFFSET = 0.5F;
    private static final int ANIMATION_DURATION = 1332;
    private static final byte NUM_POINTS = 5;
    private final List<Animation> mAnimators = new ArrayList();
    private final Ring mRing = new Ring();
    private float mRotation;
    private static final byte ARROW_WIDTH = 10;
    private static final byte ARROW_HEIGHT = 5;
    private static final float ARROW_OFFSET_ANGLE = 5.0F;
    private static final byte ARROW_WIDTH_LARGE = 12;
    private static final byte ARROW_HEIGHT_LARGE = 6;
    private static final float MAX_PROGRESS_ARC = 0.8F;
    private View mParent;
    private Animation mAnimation;
    float mRotationCount;
    private float mWidth;
    private float mHeight;
    boolean mFinishing;

    public MaterialProgressDrawable(View parent) {
        this.mParent = parent;
        this.setColorSchemeColors(COLORS);
        this.updateSizes(1);
        this.setupAnimators();
    }

    private void setSizeParameters(int progressCircleWidth, int progressCircleHeight, float centerRadius, float strokeWidth, float arrowWidth, float arrowHeight) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float screenDensity = metrics.density;
        this.mWidth = (float)progressCircleWidth * screenDensity;
        this.mHeight = (float)progressCircleHeight * screenDensity;
        this.mRing.setColorIndex(0);
        this.mRing.mPaint.setStrokeWidth(strokeWidth * screenDensity);
        this.mRing.mStrokeWidth = strokeWidth * screenDensity;
        this.mRing.mRingCenterRadius = (double)(centerRadius * screenDensity);
        this.mRing.mArrowWidth = (int)(arrowWidth * screenDensity);
        this.mRing.mArrowHeight = (int)(arrowHeight * screenDensity);
        this.mRing.setInsets((int)this.mWidth, (int)this.mHeight);
        this.invalidateSelf();
    }

    public void updateSizes(int size) {
        if (size == 0) {
            this.setSizeParameters(56, 56, 12.5F, 3.0F, 12.0F, 6.0F);
        } else {
            this.setSizeParameters(40, 40, 8.75F, 2.5F, 10.0F, 5.0F);
        }

    }

    public void showArrow(boolean show) {
        if (this.mRing.mShowArrow != show) {
            this.mRing.mShowArrow = show;
            this.invalidateSelf();
        }

    }

    public void setArrowScale(float scale) {
        if (this.mRing.mArrowScale != scale) {
            this.mRing.mArrowScale = scale;
            this.invalidateSelf();
        }

    }

    public void setStartEndTrim(float startAngle, float endAngle) {
        this.mRing.mStartTrim = startAngle;
        this.mRing.mEndTrim = endAngle;
        this.invalidateSelf();
    }

    public void setProgressRotation(float rotation) {
        this.mRing.mRotation = rotation;
        this.invalidateSelf();
    }

    public void setBackgroundColor(@ColorInt int color) {
        this.mRing.mBackgroundColor = color;
    }

    public void setColorSchemeColors(@ColorInt int... colors) {
        this.mRing.mColors = colors;
        this.mRing.setColorIndex(0);
    }

    public int getIntrinsicHeight() {
        return (int)this.mHeight;
    }

    public int getIntrinsicWidth() {
        return (int)this.mWidth;
    }

    public void draw(@NonNull Canvas c) {
        Rect bounds = this.getBounds();
        int saveCount = c.save();
        c.rotate(this.mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        this.mRing.draw(c, bounds);
        c.restoreToCount(saveCount);
    }

    public void setAlpha(int alpha) {
        this.mRing.mAlpha = alpha;
    }

    public int getAlpha() {
        return this.mRing.mAlpha;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mRing.mPaint.setColorFilter(colorFilter);
        this.invalidateSelf();
    }

    void setRotation(float rotation) {
        this.mRotation = rotation;
        this.invalidateSelf();
    }

    @SuppressLint("WrongConstant")
    public int getOpacity() {
        return -3;
    }

    public boolean isRunning() {
        List<Animation> animators = this.mAnimators;
        int N = animators.size();

        for(int i = 0; i < N; ++i) {
            Animation animator = (Animation)animators.get(i);
            if (animator.hasStarted() && !animator.hasEnded()) {
                return true;
            }
        }

        return false;
    }

    public void start() {
        this.mAnimation.reset();
        this.mRing.storeOriginals();
        if (this.mRing.mEndTrim != this.mRing.mStartTrim) {
            this.mFinishing = true;
            this.mAnimation.setDuration(666L);
            this.mParent.startAnimation(this.mAnimation);
        } else {
            this.mRing.setColorIndex(0);
            this.mRing.resetOriginals();
            this.mAnimation.setDuration(1332L);
            this.mParent.startAnimation(this.mAnimation);
        }

    }

    public void stop() {
        this.mParent.clearAnimation();
        this.mRing.setColorIndex(0);
        this.mRing.resetOriginals();
        this.showArrow(false);
        this.setRotation(0.0F);
    }

    float getMinProgressArc(Ring ring) {
        return (float)Math.toRadians((double)ring.mStrokeWidth / (6.283185307179586D * ring.mRingCenterRadius));
    }

    private int evaluateColorChange(float fraction, int startValue, int endValue) {
        int startA = startValue >> 24 & 255;
        int startR = startValue >> 16 & 255;
        int startG = startValue >> 8 & 255;
        int startB = startValue & 255;
        int endA = endValue >> 24 & 255;
        int endR = endValue >> 16 & 255;
        int endG = endValue >> 8 & 255;
        int endB = endValue & 255;
        return startA + (int)(fraction * (float)(endA - startA)) << 24 | startR + (int)(fraction * (float)(endR - startR)) << 16 | startG + (int)(fraction * (float)(endG - startG)) << 8 | startB + (int)(fraction * (float)(endB - startB));
    }

    void updateRingColor(float interpolatedTime, Ring ring) {
        if (interpolatedTime > 0.75F) {
            ring.mCurrentColor = this.evaluateColorChange((interpolatedTime - 0.75F) / 0.25F, ring.getStartingColor(), ring.getNextColor());
        }

    }

    void applyFinishTranslation(float interpolatedTime, Ring ring) {
        this.updateRingColor(interpolatedTime, ring);
        float targetRotation = (float)(Math.floor((double)(ring.mStartingRotation / 0.8F)) + 1.0D);
        float minProgressArc = this.getMinProgressArc(ring);
        float startTrim = ring.mStartingStartTrim + (ring.mStartingEndTrim - minProgressArc - ring.mStartingStartTrim) * interpolatedTime;
        this.setStartEndTrim(startTrim, ring.mStartingEndTrim);
        float rotation = ring.mStartingRotation + (targetRotation - ring.mStartingRotation) * interpolatedTime;
        this.setProgressRotation(rotation);
    }

    private void setupAnimators() {
        final Ring ring = this.mRing;
        Animation animation = new Animation() {
            public void applyTransformation(float interpolatedTime, Transformation t) {
                if (MaterialProgressDrawable.this.mFinishing) {
                    MaterialProgressDrawable.this.applyFinishTranslation(interpolatedTime, ring);
                } else {
                    float minProgressArc = MaterialProgressDrawable.this.getMinProgressArc(ring);
                    float startingEndTrim = ring.mStartingEndTrim;
                    float startingTrim = ring.mStartingStartTrim;
                    float startingRotation = ring.mStartingRotation;
                    MaterialProgressDrawable.this.updateRingColor(interpolatedTime, ring);
                    float groupRotation;
                    if (interpolatedTime <= 0.5F) {
                        groupRotation = interpolatedTime / 0.5F;
                        ring.mStartTrim = startingTrim + (0.8F - minProgressArc) * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(groupRotation);
                    }

                    if (interpolatedTime > 0.5F) {
                        groupRotation = 0.8F - minProgressArc;
                        float scaledTime = (interpolatedTime - 0.5F) / 0.5F;
                        ring.mEndTrim = startingEndTrim + groupRotation * MaterialProgressDrawable.MATERIAL_INTERPOLATOR.getInterpolation(scaledTime);
                    }

                    MaterialProgressDrawable.this.setProgressRotation(startingRotation + 0.25F * interpolatedTime);
                    groupRotation = 216.0F * interpolatedTime + 1080.0F * (MaterialProgressDrawable.this.mRotationCount / 5.0F);
                    MaterialProgressDrawable.this.setRotation(groupRotation);
                }

            }
        };
        animation.setRepeatCount(-1);
        animation.setRepeatMode(1);
        animation.setInterpolator(LINEAR_INTERPOLATOR);
        animation.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                MaterialProgressDrawable.this.mRotationCount = 0.0F;
            }

            public void onAnimationEnd(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
                ring.storeOriginals();
                ring.goToNextColor();
                ring.mStartTrim = ring.mEndTrim;
                if (MaterialProgressDrawable.this.mFinishing) {
                    MaterialProgressDrawable.this.mFinishing = false;
                    animation.setDuration(1332L);
                    MaterialProgressDrawable.this.showArrow(false);
                } else {
                    MaterialProgressDrawable.this.mRotationCount = (MaterialProgressDrawable.this.mRotationCount + 1.0F) % 5.0F;
                }

            }
        });
        this.mAnimation = animation;
    }

    private class Ring {
        final RectF mTempBounds = new RectF();
        final Paint mPaint = new Paint();
        final Paint mArrowPaint = new Paint();
        float mStartTrim = 0.0F;
        float mEndTrim = 0.0F;
        float mRotation = 0.0F;
        float mStrokeWidth = 5.0F;
        float mStrokeInset = 2.5F;
        int[] mColors;
        int mColorIndex;
        float mStartingStartTrim;
        float mStartingEndTrim;
        float mStartingRotation;
        boolean mShowArrow;
        Path mArrow;
        float mArrowScale;
        double mRingCenterRadius;
        int mArrowWidth;
        int mArrowHeight;
        int mAlpha;
        final Paint mCirclePaint = new Paint(1);
        int mBackgroundColor;
        int mCurrentColor;

        Ring() {
            this.mPaint.setStrokeCap(Cap.SQUARE);
            this.mPaint.setAntiAlias(true);
            this.mPaint.setStyle(Style.STROKE);
            this.mArrowPaint.setStyle(Style.FILL);
            this.mArrowPaint.setAntiAlias(true);
        }

        public void draw(Canvas c, Rect bounds) {
            RectF arcBounds = this.mTempBounds;
            arcBounds.set(bounds);
            arcBounds.inset(this.mStrokeInset, this.mStrokeInset);
            float startAngle = (this.mStartTrim + this.mRotation) * 360.0F;
            float endAngle = (this.mEndTrim + this.mRotation) * 360.0F;
            float sweepAngle = endAngle - startAngle;
            if (sweepAngle != 0.0F) {
                this.mPaint.setColor(this.mCurrentColor);
                c.drawArc(arcBounds, startAngle, sweepAngle, false, this.mPaint);
            }

            this.drawTriangle(c, startAngle, sweepAngle, bounds);
            if (this.mAlpha < 255) {
                this.mCirclePaint.setColor(this.mBackgroundColor);
                this.mCirclePaint.setAlpha(255 - this.mAlpha);
                c.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), (float)bounds.width() / 2.0F, this.mCirclePaint);
            }

        }

        private void drawTriangle(Canvas c, float startAngle, float sweepAngle, Rect bounds) {
            if (this.mShowArrow) {
                if (this.mArrow == null) {
                    this.mArrow = new Path();
                    this.mArrow.setFillType(FillType.EVEN_ODD);
                } else {
                    this.mArrow.reset();
                }

                float inset = (float)((int)this.mStrokeInset / 2) * this.mArrowScale;
                float x = (float)(this.mRingCenterRadius * Math.cos(0.0D) + (double)bounds.exactCenterX());
                float y = (float)(this.mRingCenterRadius * Math.sin(0.0D) + (double)bounds.exactCenterY());
                this.mArrow.moveTo(0.0F, 0.0F);
                this.mArrow.lineTo((float)this.mArrowWidth * this.mArrowScale, 0.0F);
                this.mArrow.lineTo((float)this.mArrowWidth * this.mArrowScale / 2.0F, (float)this.mArrowHeight * this.mArrowScale);
                this.mArrow.offset(x - inset, y);
                this.mArrow.close();
                this.mArrowPaint.setColor(this.mCurrentColor);
                c.rotate(startAngle + sweepAngle - 5.0F, bounds.exactCenterX(), bounds.exactCenterY());
                c.drawPath(this.mArrow, this.mArrowPaint);
            }

        }

        public void setColorIndex(int index) {
            this.mColorIndex = index;
            this.mCurrentColor = this.mColors[this.mColorIndex];
        }

        public int getNextColor() {
            return this.mColors[this.getNextColorIndex()];
        }

        private int getNextColorIndex() {
            return (this.mColorIndex + 1) % this.mColors.length;
        }

        public void goToNextColor() {
            this.setColorIndex(this.getNextColorIndex());
        }

        public int getStartingColor() {
            return this.mColors[this.mColorIndex];
        }

        public void setInsets(int width, int height) {
            float minEdge = (float)Math.min(width, height);
            float insets;
            if (!(this.mRingCenterRadius <= 0.0D) && !(minEdge < 0.0F)) {
                insets = (float)((double)(minEdge / 2.0F) - this.mRingCenterRadius);
            } else {
                insets = (float)Math.ceil((double)(this.mStrokeWidth / 2.0F));
            }

            this.mStrokeInset = insets;
        }

        public void storeOriginals() {
            this.mStartingStartTrim = this.mStartTrim;
            this.mStartingEndTrim = this.mEndTrim;
            this.mStartingRotation = this.mRotation;
        }

        public void resetOriginals() {
            this.mStartingStartTrim = 0.0F;
            this.mStartingEndTrim = 0.0F;
            this.mStartingRotation = 0.0F;
            this.mStartTrim = 0.0F;
            this.mEndTrim = 0.0F;
            this.mRotation = 0.0F;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgressDrawableSize {
    }
}
