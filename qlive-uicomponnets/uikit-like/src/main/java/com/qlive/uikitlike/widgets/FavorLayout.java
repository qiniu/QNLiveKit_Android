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
        //?????? ?????? ????????????
        lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(CENTER_HORIZONTAL, TRUE); //?????????TRUE ????????? ??????true
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
     * ??????
     * ?????????????????????
     */
    public void addFavor() {
        ImageView imageView = new ImageView(getContext());
        // ???????????????
        imageView.setImageDrawable(RandomUtils.getRandomElement(loves));

        if (ancherPoint == null) {
            imageView.setLayoutParams(lp);
        } else {
            imageView.setX(ancherPoint.x - getX());
            imageView.setY(ancherPoint.y - getY());
        }

        addView(imageView);
        Log.e(TAG, "addFavor: " + "add??????view???:" + getChildCount());

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
     * ????????????
     */
    private Animator getAnimator(View target) {
        AnimatorSet set = getEnterAnimtor(target);

        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet finalSet = new AnimatorSet();

        finalSet.playTogether(set, bezierValueAnimator);
        finalSet.setInterpolator(RandomUtils.getRandomElement(interpolators));//??????????????????
        finalSet.setTarget(target);
        return finalSet;
    }


    /**
     * ??????????????????
     * ?????? ?????????????????????
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
            // ?????????
            float x = ancherPoint.x - getX();
            float y = ancherPoint.y - getY();
            pointF.x = x - favorWidth / 2 + RandomUtils.getRandomInt(favorWidth);
            pointF.y = y - favorHeight / 4f - RandomUtils.getRandomInt(favorHeight / 4);

        } else {
            //??????100 ??????????????? x???????????????
            pointF.x = RandomUtils.getRandomInt(mWidth - 100);
            //???Y?????? ?????????????????????????????? ?????????????????????,??????Y?????????????????????
            pointF.y = RandomUtils.getRandomInt(mHeight - 100) / 2;
        }

        return pointF;
    }

    private PointF getPointHight() {
        PointF pointF = new PointF();
        if (ancherView != null && favorWidth != -1 && favorHeight != -1 && ancherPoint != null) {
            // ?????????
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
            // ?????????
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
     * ???????????????????????????
     */
    private ValueAnimator getBezierValueAnimator(View target) {

        //???????????????BezierEvaluator
        BezierEvaluator evaluator = new BezierEvaluator(getPointLow(), getPointHight());

        // ???????????????????????????
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, ancherPoint == null ? startPoint : new PointF(ancherPoint.x - getX(), ancherPoint.y - getY
                ()), getEndPoint());
        animator.addUpdateListener(new BezierListener(target));
        animator.setTarget(target);
        animator.setDuration(3000);
        return animator;
    }

    /**
     * ????????????????????????
     */
    public void setFavors(List<Drawable> items) {
        loves.clear();
        loves.addAll(items);
        if (items.size() == 0) {
            throw new UnsupportedOperationException("??????????????????????????????");
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
            Log.v(TAG, "removeView??????view???:" + getChildCount());
            if (mAnimator.size() <= 0) {
                if(faverListener!=null){
                    faverListener.onEnd();
                }
                Log.v(TAG, "??????????????????");
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

