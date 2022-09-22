package com.qlive.uikitcore.floating.uitls

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import com.qlive.uikitcore.floating.OnFloatAnimator
import com.qlive.uikitcore.floating.SidePattern
import kotlin.math.min
/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function: 悬浮窗使用工具类
 * @date: 2019-06-27  15:22
 * 修改原作者 https://github.com/princekin-f/EasyFloat 动画实现
 */
class DefaultAnimator : OnFloatAnimator {
    override fun enterAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? = getAnimator(view, params, windowManager, sidePattern, false)

    override fun exitAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? = getAnimator(view, params, windowManager, sidePattern, true)

    private fun getAnimator(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern,
        isExit: Boolean
    ): Animator {
        val triple = initValue(view, params, windowManager, sidePattern)
        // 退出动画的起始值、终点值，与入场动画相反
        val start = if (isExit) triple.second else triple.first
        val end = if (isExit) triple.first else triple.second
        return ValueAnimator.ofInt(start, end).apply {
            addUpdateListener {
                try {
                    val value = it.animatedValue as Int
                    if (triple.third) params.x = value else params.y = value
                    // 动画执行过程中页面关闭，出现异常
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    cancel()
                }
            }
        }
    }

    /**
     * 计算边距，起始坐标等
     */
    private fun initValue(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Triple<Int, Int, Boolean> {
        val parentRect = Rect()
        windowManager.defaultDisplay.getRectSize(parentRect)
        // 浮窗各边到窗口边框的距离
        val leftDistance = params.x
        val rightDistance = parentRect.right - (leftDistance + view.right)
        val topDistance = params.y
        val bottomDistance = parentRect.bottom - (topDistance + view.bottom)
        // 水平、垂直方向的距离最小值
        val minX = min(leftDistance, rightDistance)
        val minY = min(topDistance, bottomDistance)
        // 水平位移，哪边距离屏幕近，从哪侧移动
        val isHorizontal: Boolean = true
        val endValue: Int = params.x
        val startValue: Int =if (leftDistance < rightDistance) -view.right else parentRect.right
        return Triple(startValue, endValue, isHorizontal)
    }
}