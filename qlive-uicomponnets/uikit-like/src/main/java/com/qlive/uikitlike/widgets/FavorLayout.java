package com.qlive.uikitlike.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class FavorLayout extends RelativeLayout {
    private static final String TAG = FavorLayout.class.getSimpleName();
    private int mHeight;
    private int mWidth;

    private LayoutParams lp;
    private final List<Drawable> loves = new ArrayList<Drawable>();

    private final List<Interpolator> interpolators = new ArrayList<Interpolator>() {
        {
            add(new LinearInterpolator());
            add(new AccelerateInterpolator());
            add(new DecelerateInterpolator());
            //add(new AccelerateDecelerateInterpolator());
        }
    };
    private PointF startPoint = new PointF();
    private PointF ancherPoint;
    private View ancherView;
    private int favorWidth = -1, favorHeight = -1;

    public FaverListener faverListener=null;

    public FavorLayout(Context context) {
        super(context);
        init();
    }

    public FavorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FavorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

    }

    private void init() {
        //底部 并且 水平居中
        lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(CENTER_HORIZONTAL, TRUE); //这里的TRUE 要注意 不是true
        lp.addRule(ALIGN_PARENT_BOTTOM, TRUE);
    }

    public void setAncher(final View view) {
        ancherView = view;
        resetAncherPoint();
    }

    private void resetAncherPoint() {
        if (ancherView != null) {
            int[] outLocation = new int[2];
            ancherView.getLocationOnScreen(outLocation);
            float x = outLocation[0] + (ancherView.getWidth()) / 2;
            float y = outLocation[1];
            ancherPoint = new PointF(x, y);
            Log.d("FavorLayout", "ancherPoint" + x + " " + y);
        }
    }

    private List<Animator> mAnimator = new ArrayList<Animator>();

    /**
     * 点赞
     * 对外暴露的方法
     */
    public void addFavor() {
        ImageView imageView = new ImageView(getContext());
        // 随机选一个
        imageView.setImageDrawable(RandomUtils.getRandomElement(loves));

        if (ancherPoint == null) {
            imageView.setLayoutParams(lp);
        } else {
            imageView.setX(ancherPoint.x - getX());
            imageView.setY(ancherPoint.y - getY());
        }

        addView(imageView);
        Log.e(TAG, "addFavor: " + "add后子view数:" + getChildCount());

        Animator set = getAnimator(imageView);
        mAnimator.add(set);
        set.addListener(new AnimEndListener(imageView));
        set.start();
    }

    private AutoStartRun autoStartRun = new AutoStartRun();

    public void clear() {
        autoStartRun.stop();
        List<Animator> mAnimatorNew = new ArrayList<Animator>(mAnimator);
        for (Animator animator : mAnimatorNew) {
            animator.cancel();
        }
    }

    public void start(int count, int timeDiff) {
        autoStartRun.start(count, timeDiff);
    }


    /**
     * 设置动画
     */
    private Animator getAnimator(View target) {
        AnimatorSet set = getEnterAnimtor(target);

        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet finalSet = new AnimatorSet();

        finalSet.playTogether(set, bezierValueAnimator);
        finalSet.setInterpolator(RandomUtils.getRandomElement(interpolators));//实现随机变速
        finalSet.setTarget(target);
        return finalSet;
    }


    /**
     * 设置初始动画
     * 渐变 并且横纵向放大
     */
    private AnimatorSet getEnterAnimtor(final View target) {

        ObjectAnimator alpha = ObjectAnimator.ofFloat(target, View.ALPHA, 0.2f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0f, 1f);
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(500);
        enter.setInterpolator(new LinearInterpolator());
        enter.playTogether(alpha, scaleX, scaleY);
        enter.setTarget(target);
        return enter;
    }

    public void setFavorWidthHeight(int width, int height) {
        this.favorWidth = dp2px(getContext(), width);
        this.favorHeight = dp2px(getContext(), height);
    }

    private PointF getPointLow() {
        PointF pointF = new PointF();
        if (ancherView != null && favorWidth != -1 && favorHeight != -1 && ancherPoint != null) {
            // 中心点
            float x = ancherPoint.x - getX();
            float y = ancherPoint.y - getY();
            pointF.x = x - favorWidth / 2 + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight / 4f - RandomUtils.getRandomInt(favorHeight / 4);

        } else {
            //减去100 是为了控制 x轴活动范围
            pointF.x = RandomUtils.getRandomInt(mWidth - 100);
            //再Y轴上 为了确保第二个控制点 在第一个点之上,我把Y分成了上下两半
            pointF.y = RandomUtils.getRandomInt(mHeight - 100) / 2;
        }

        return pointF;
    }

    private PointF getPointHight() {
        PointF pointF = new PointF();
        if (ancherView != null && favorWidth != -1 && favorHeight != -1 && ancherPoint != null) {
            // 中心点
            float x = ancherPoint.x - getX();
            float y = ancherPoint.y - getY();
            pointF.x = x - favorWidth / 2 + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight / 2 - RandomUtils.getRandomInt(favorHeight / 4);

        } else {
            pointF.x = RandomUtils.getRandomInt(mWidth - 100);
            pointF.y = RandomUtils.getRandomInt(mHeight - 100);
        }

        return pointF;
    }

    private PointF getEndPoint() {
        PointF pointF = new PointF();
        if (ancherView != null && favorWidth != -1 && favorHeight != -1 && ancherPoint != null) {
            // 中心点
            float x = ancherPoint.x - getX();
            float y = ancherPoint.y - getY();
            pointF.x = x - favorWidth / 2 + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight;

        } else {
            pointF.set(RandomUtils.getRandomInt(getWidth()), 0);
        }

        return pointF;
    }

    /**
     * 获取贝塞尔曲线动画
     */
    private ValueAnimator getBezierValueAnimator(View target) {

        //初始化一个BezierEvaluator
        BezierEvaluator evaluator = new BezierEvaluator(getPointLow(), getPointHight());

        // 起点固定，终点随机
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, ancherPoint == null ? startPoint : new PointF(ancherPoint.x - getX(), ancherPoint.y - getY
                ()), getEndPoint());
        animator.addUpdateListener(new BezierListener(target));
        animator.setTarget(target);
        animator.setDuration(3000);
        return animator;
    }

    /**
     * 设置点赞效果集合
     */
    public void setFavors(List<Drawable> items) {
        loves.clear();
        loves.addAll(items);
        if (items.size() == 0) {
            throw new UnsupportedOperationException("点赞效果图片不能为空");
        }
        resetAncherPoint();
    }


    private class AnimEndListener extends AnimatorListenerAdapter {
        private final View target;

        public AnimEndListener(View target) {
            this.target = target;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            checkFinish(animation);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            checkFinish(animation);
        }

        private void checkFinish(Animator animation) {
            mAnimator.remove(animation);
            removeView((target));
            Log.v(TAG, "removeView后子view数:" + getChildCount());
            if (mAnimator.size() <= 0) {
                if(faverListener!=null){
                    faverListener.onEnd();
                }
                Log.v(TAG, "点赞动画结束");
            }
        }
    }

    class AutoStartRun implements Runnable {
        public int count = 0;
        public int timeDiff = 0;
        private int index = 0;
        public boolean isRun = false;

        public void start(int count, int timeDiff) {
            this.count = count;
            this.timeDiff = timeDiff;
            index = 0;
            if (!isRun) {
                if(faverListener!=null){
                    faverListener.onStart();
                }
                isRun = true;
                run();
            }
        }

        public void stop() {
            isRun = false;
            count = 0;
            timeDiff = 0;
            index = 0;
            removeCallbacks(this);
        }

        @Override
        public void run() {
            if (index < count && isRun) {
                index++;
                addFavor();
                postDelayed(this, timeDiff);
            } else {
                isRun = false;
            }
        }
    }

    private static int dp2px(Context ctx, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }


}

