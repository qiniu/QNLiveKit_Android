package com.qlive.uikitlike.widgets;

import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.View;

/**
 * Description: 动画监听，这里控制位置，真正实现动画
 */
public class BezierListener implements ValueAnimator.AnimatorUpdateListener {

    private final View target;

    public BezierListener(View target) {
        this.target = target;
    }
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        //这里获取到贝塞尔曲线计算出来的的x y值 赋值给view 这样就能让爱心随着曲线走啦
        PointF pointF = (PointF) animation.getAnimatedValue();
        target.setX(pointF.x);
        target.setY(pointF.y);
        // 这里偷个懒,顺便做一个alpha动画,这样alpha渐变也完成啦
        target.setAlpha(1-animation.getAnimatedFraction());
    }
}

