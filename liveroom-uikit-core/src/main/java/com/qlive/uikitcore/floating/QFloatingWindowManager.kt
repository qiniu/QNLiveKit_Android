package com.qlive.uikitcore.floating

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.qlive.uikitcore.floating.uitls.DisplayUtils
import com.qlive.uikitcore.floating.uitls.LifecycleUtils
import kotlin.math.max
import kotlin.math.min

/**
 * @author: liuzhenfeng
 * @github：https://github.com/princekin-f
 * @function: 悬浮窗使用工具类
 * @date: 2019-06-27  15:22
 * 修改原作者 https://github.com/princekin-f/EasyFloat
 */
@SuppressLint("ViewConstructor")
internal class QFloatingWindowManager(
    private val activity: Activity,
    val config: QFloatConfig,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(activity, attrs, defStyleAttr) {

    private val windowManager: WindowManager =
        context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    private var enterAnimator: Animator? = null
    private var lastLayoutMeasureWidth = -1
    private var lastLayoutMeasureHeight = -1
    private val mQFloatCallbackWrap = QFloatCallbackWrap()
    val params: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                // 设置窗口类型为应用子窗口，和PopupWindow同类型
                type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                // 子窗口必须和创建它的Activity的windowToken绑定
                token = activity.window.decorView.windowToken
            } else {
                // 系统全局窗口，可覆盖在任何应用之上，以及单独显示在桌面上
                // 安卓6.0 以后，全局的Window类别，必须使用TYPE_APPLICATION_OVERLAY
                type =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            gravity = Gravity.START or Gravity.TOP
            // 设置浮窗以外的触摸事件可以传递给后面的窗口、不自动获取焦点
            flags = if (config.immersionStatusBar)
            // 没有边界限制，允许窗口扩展到屏幕外
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            else WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            this.width =
                if (config.widthMatch) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT
            this.height =
                if (config.heightMatch) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT
            if (config.immersionStatusBar && config.heightMatch) {
                height = DisplayUtils.getScreenHeight(context)
            }
            // 如若设置了固定坐标，直接定位
            if (config.locationPair != Pair(0, 0)) {
                x = config.locationPair.first
                y = config.locationPair.second
            }
        }
    }

    // 是否正在被拖拽
    var isDrag: Boolean = false

    // 是否正在执行动画
    var isAnim: Boolean = false

    // 是否显示
    var isShow: Boolean = false
        private set
    var isCreated = false
        private set

    fun addQFloatCallback(callback: QFloatCallback) {
        mQFloatCallbackWrap.callbacks.add(callback)
    }

    fun removeQFloatCallback(callback: QFloatCallback) {
        mQFloatCallbackWrap.callbacks.remove(callback)
    }

    init {
        mQFloatCallbackWrap.callbacks.add(object : QFloatCallback {
            override fun onCreate(view: View) {
                isCreated = true
            }

            override fun onDismiss() {
                super.onDismiss()
                isCreated = false
            }

            override fun onShow(view: View) {
                super.onShow(view)
                isShow = true
            }

            override fun onHide(view: View) {
                super.onHide(view)
                isShow = false
            }

            override fun onDrag(view: View, event: MotionEvent) {
                super.onDrag(view, event)
                isDrag = true
            }

            override fun onDragEnd(view: View) {
                super.onDragEnd(view)
                isDrag = false
            }
        })
    }

    fun create(): Boolean {
        return try {
            val floatingView = LayoutInflater.from(context)
                .inflate(config.layoutID, this, true)
            // 为了避免创建的时候闪一下，我们先隐藏视图，不能直接设置GONE，否则定位会出现问题
            floatingView.visibility = View.INVISIBLE
            // 将frameLayout添加到系统windowManager中
            windowManager.addView(this, params)
            this.apply {
                // 监听frameLayout布局完成
                viewTreeObserver?.addOnGlobalLayoutListener {
                    val filterInvalidVal =
                        lastLayoutMeasureWidth == -1 || lastLayoutMeasureHeight == -1
                    val filterEqualVal =
                        lastLayoutMeasureWidth == this.measuredWidth && lastLayoutMeasureHeight == this.measuredHeight
                    if (filterInvalidVal || filterEqualVal) {
                        return@addOnGlobalLayoutListener
                    }
                    // 水平方向
                    if (config.layoutChangedGravity.and(Gravity.START) == Gravity.START) {
                        // ignore

                    } else if (config.layoutChangedGravity.and(Gravity.END) == Gravity.END) {
                        val diffChangedSize = this.measuredWidth - lastLayoutMeasureWidth
                        params.x = params.x - diffChangedSize

                    } else if (config.layoutChangedGravity.and(Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL
                        || config.layoutChangedGravity.and(Gravity.CENTER) == Gravity.CENTER
                    ) {
                        val diffChangedCenter = lastLayoutMeasureWidth / 2 - this.measuredWidth / 2
                        params.x += diffChangedCenter
                    }

                    // 垂直方向
                    if (config.layoutChangedGravity.and(Gravity.TOP) == Gravity.TOP) {
                        // ignore

                    } else if (config.layoutChangedGravity.and(Gravity.BOTTOM) == Gravity.BOTTOM) {
                        val diffChangedSize = this.measuredHeight - lastLayoutMeasureHeight
                        params.y = params.y - diffChangedSize

                    } else if (config.layoutChangedGravity.and(Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL
                        || config.layoutChangedGravity.and(Gravity.CENTER) == Gravity.CENTER
                    ) {
                        val diffChangedCenter =
                            lastLayoutMeasureHeight / 2 - this.measuredHeight / 2
                        params.y += diffChangedCenter
                    }
                    lastLayoutMeasureWidth = this.measuredWidth
                    lastLayoutMeasureHeight = this.measuredHeight
                    // 更新浮窗位置信息
                    windowManager.updateViewLayout(this, params)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun dispatchCreate() {
        mQFloatCallbackWrap.onCreate(getChildAt(0))
    }

    fun clear() {
        mQFloatCallbackWrap.callbacks.clear()
        removeAllViews()
    }

    private var isFirstLayout = false
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 初次绘制完成的时候，需要设置对齐方式、坐标偏移量、入场动画
        if (!isFirstLayout) {
            isFirstLayout = true
            setGravity(this)
            lastLayoutMeasureWidth = this.measuredWidth ?: -1
            lastLayoutMeasureHeight = this.measuredHeight ?: -1
            config.apply {
                // 如果设置了过滤当前页，或者后台显示前台创建、前台显示后台创建，隐藏浮窗，否则执行入场动画
                if ((showPattern == ShowPattern.BACKGROUND && LifecycleUtils.isForeGround())
                    || (showPattern == ShowPattern.FOREGROUND && !LifecycleUtils.isForeGround())
                ) {
                    setVisible(View.GONE)
                } else enterAnim(getChildAt(0))
                // 设置callbacks
            }
        }
    }

    /**
     * 设置浮窗的可见性
     */
    fun setVisible(visible: Int) {
        if (!isCreated) return
        this.visibility = visible
        val view = this.getChildAt(0)
        if (visible == View.VISIBLE) {
            mQFloatCallbackWrap.onShow(view)
        } else {
            mQFloatCallbackWrap.onHide(view)
        }
    }

    private fun setGravity(view: View?) {
        if (config.locationPair != Pair(0, 0) || view == null) return
        val parentRect = Rect()
        // 获取浮窗所在的矩形
        windowManager.defaultDisplay.getRectSize(parentRect)
        val location = IntArray(2)
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location)
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        val statusBarHeight = if (location[1] > params.y) DisplayUtils.statusBarHeight(view) else 0
        val parentBottom = DisplayUtils.rejectedNavHeight(context) - statusBarHeight
        when (config.gravity) {
            // 右上
            Gravity.END, Gravity.END or Gravity.TOP, Gravity.RIGHT, Gravity.RIGHT or Gravity.TOP ->
                params.x = parentRect.right - view.width
            // 左下
            Gravity.START or Gravity.BOTTOM, Gravity.BOTTOM, Gravity.LEFT or Gravity.BOTTOM ->
                params.y = parentBottom - view.height
            // 右下
            Gravity.END or Gravity.BOTTOM, Gravity.RIGHT or Gravity.BOTTOM -> {
                params.x = parentRect.right - view.width
                params.y = parentBottom - view.height
            }
            // 居中
            Gravity.CENTER -> {
                params.x = (parentRect.right - view.width).shr(1)
                params.y = (parentBottom - view.height).shr(1)
            }
            // 上中
            Gravity.CENTER_HORIZONTAL, Gravity.TOP or Gravity.CENTER_HORIZONTAL ->
                params.x = (parentRect.right - view.width).shr(1)
            // 下中
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL -> {
                params.x = (parentRect.right - view.width).shr(1)
                params.y = parentBottom - view.height
            }
            // 左中
            Gravity.CENTER_VERTICAL, Gravity.START or Gravity.CENTER_VERTICAL, Gravity.LEFT or Gravity.CENTER_VERTICAL ->
                params.y = (parentBottom - view.height).shr(1)
            // 右中
            Gravity.END or Gravity.CENTER_VERTICAL, Gravity.RIGHT or Gravity.CENTER_VERTICAL -> {
                params.x = parentRect.right - view.width
                params.y = (parentBottom - view.height).shr(1)
            }
            // 其他情况，均视为左上
            else -> {
            }
        }
        // 设置偏移量
        params.x += config.offsetPair.first
        params.y += config.offsetPair.second

        if (config.immersionStatusBar) {
            if (config.showPattern != ShowPattern.CURRENT_ACTIVITY) {
                params.y -= statusBarHeight
            }
        } else {
            if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                params.y += statusBarHeight
            }
        }
        // 更新浮窗位置信息
        windowManager.updateViewLayout(view, params)
    }

    private fun enterAnim(floatingView: View) {

        fun enterAnim(): Animator? =
            config.floatAnimator?.enterAnim(this, params, windowManager, config.sidePattern)
        if (!isCreated) return
        enterAnimator = enterAnim()?.apply {
            // 可以延伸到屏幕外，动画结束按需去除该属性，不然旋转屏幕可能置于屏幕外部
            params.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    isAnim = false
                    if (!config.immersionStatusBar) {
                        // 不需要延伸到屏幕外了，防止屏幕旋转的时候，浮窗处于屏幕外
                        params.flags =
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {
                    floatingView.visibility = View.VISIBLE
                    isAnim = true
                }
            })
            start()
        }
        if (enterAnimator == null) {
            floatingView.visibility = View.VISIBLE
            windowManager.updateViewLayout(this, params)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        Log.d("onInterceptTouchEvent", "ev" + event?.action)
        if (event != null)
            updateFloat(this, event, windowManager, params)
        //return true
        return isDrag || super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) updateFloat(this, event, windowManager, params)
        return isDrag || super.onTouchEvent(event)
    }

    private fun remove(force: Boolean = false) = try {
        // removeView是异步删除，在Activity销毁的时候会导致窗口泄漏，所以使用removeViewImmediate直接删除view
        windowManager.run {
            if (force) removeViewImmediate(this@QFloatingWindowManager) else removeView(this@QFloatingWindowManager)
        }
        mQFloatCallbackWrap.onDismiss()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private fun exitAnim() {
        if (isCreated) return
        enterAnimator?.cancel()
        fun exitAnim(): Animator? =
            config.floatAnimator?.exitAnim(this, params, windowManager, config.sidePattern)

        val animator: Animator? = exitAnim()
        if (animator == null) remove() else {
            if (isAnim) return
            isAnim = true
            params.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) = remove()

                override fun onAnimationCancel(animation: Animator?) {}

                override fun onAnimationStart(animation: Animator?) {}
            })
            animator.start()
        }
    }

    /**
     * 关闭浮窗，执行浮窗的退出动画
     */
    fun dismiss(force: Boolean = false) {
        if (force) remove(force) else exitAnim()
    }

    fun updateFloat(x: Int = -1, y: Int = -1, width: Int = -1, height: Int = -1) {
        this.let {
            if (x == -1 && y == -1 && width == -1 && height == -1) {
                // 未指定具体坐标，执行吸附动画
                it.postDelayed({ updateFloat(it, params, windowManager) }, 200)
            } else {
                if (x != -1) params.x = x
                if (y != -1) params.y = y
                if (width != -1) params.width = width
                if (height != -1) params.height = height
                windowManager.updateViewLayout(it, params)
            }
        }
    }

    // 悬浮的父布局高度、宽度
    private var parentHeight = 0
    private var parentWidth = 0

    // 四周坐标边界值
    private var leftBorder = 0
    private var topBorder = 0
    private var rightBorder = 0
    private var bottomBorder = 0

    // 起点坐标
    private var lastX = 0f
    private var lastY = 0f

    // 浮窗各边距离父布局的距离
    private var leftDistance = 0
    private var rightDistance = 0
    private var topDistance = 0
    private var bottomDistance = 0

    // x轴、y轴的最小距离值
    private var minX = 0
    private var minY = 0
    private val location = IntArray(2)
    private var statusBarHeight = 0

    // 屏幕可用高度 - 浮窗自身高度 的剩余高度
    private var emptyHeight = 0

    /**
     * 根据吸附模式，实现相应的拖拽效果
     */
    private fun updateFloat(
        view: View,
        event: MotionEvent,
        windowManager: WindowManager,
        params: WindowManager.LayoutParams
    ) {
        mQFloatCallbackWrap.onTouchEvent(view, event)
        // 不可拖拽、或者正在执行动画，不做处理
        if (!config.dragEnable || isAnim) {
            isDrag = false
            return
        }

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isDrag = false
                // 记录触摸点的位置
                lastX = event.rawX
                lastY = event.rawY
                // 初始化一些边界数据
                initBoarderValue(view, params)
            }

            MotionEvent.ACTION_MOVE -> {
                // 过滤边界值之外的拖拽
                if (event.rawX < leftBorder || event.rawX > rightBorder + view.width
                    || event.rawY < topBorder || event.rawY > bottomBorder + view.height
                ) return

                // 移动值 = 本次触摸值 - 上次触摸值
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                // 忽略过小的移动，防止点击无效
                if (!isDrag && dx * dx + dy * dy < 81) return
                isDrag = true

                var x = params.x + dx.toInt()
                var y = params.y + dy.toInt()
                // 检测浮窗是否到达边缘
                x = when {
                    x < leftBorder -> leftBorder
                    x > rightBorder -> rightBorder
                    else -> x
                }

                if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
                    // 单页面浮窗，设置状态栏不沉浸时，最小高度为状态栏高度
                    if (y < statusBarHeight(view) && !config.immersionStatusBar) y =
                        statusBarHeight(view)
                }

                y = when {
                    y < topBorder -> topBorder
                    // 状态栏沉浸时，最小高度为-statusBarHeight，反之最小高度为0
                    y < 0 -> if (config.immersionStatusBar) {
                        if (y < -statusBarHeight) -statusBarHeight else y
                    } else 0
                    y > bottomBorder -> bottomBorder
                    else -> y
                }
                // 重新设置坐标信息
                params.x = x
                params.y = y
                windowManager.updateViewLayout(view, params)
                mQFloatCallbackWrap.onDrag(view, event)
                // 更新上次触摸点的数据
                lastX = event.rawX
                lastY = event.rawY
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isDrag) return
                // 回调拖拽事件的ACTION_UP
                mQFloatCallbackWrap.onDrag(view, event)
                when (config.sidePattern) {
                    SidePattern.RESULT_HORIZONTAL -> sideAnim(view, params, windowManager)
                    else -> {
                        mQFloatCallbackWrap.onDragEnd(view)
                    }
                }
            }
            else -> return
        }
    }

    /**
     * 初始化边界值等数据
     */
    private fun initBoarderValue(view: View, params: WindowManager.LayoutParams) {
        // 屏幕宽高需要每次获取，可能会有屏幕旋转、虚拟导航栏的状态变化
        parentWidth = DisplayUtils.getScreenWidth(context)
        parentHeight = DisplayUtils.rejectedNavHeight(context)
        // 获取在整个屏幕内的绝对坐标
        view.getLocationOnScreen(location)
        // 通过绝对高度和相对高度比较，判断包含顶部状态栏
        statusBarHeight = if (location[1] > params.y) statusBarHeight(view) else 0
        emptyHeight = parentHeight - view.height - statusBarHeight

        leftBorder = max(0, config.leftBorder)
        rightBorder = min(parentWidth, config.rightBorder) - view.width
        topBorder = if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 单页面浮窗，坐标屏幕顶部计算
            if (config.immersionStatusBar) config.topBorder
            else config.topBorder + statusBarHeight(view)
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            if (config.immersionStatusBar) config.topBorder - statusBarHeight(view) else config.topBorder
        }
        bottomBorder = if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            // 单页面浮窗，坐标屏幕顶部计算
            if (config.immersionStatusBar)
                min(emptyHeight, config.bottomBorder - view.height)
            else
                min(emptyHeight, config.bottomBorder + statusBarHeight(view) - view.height)
        } else {
            // 系统浮窗，坐标从状态栏底部开始，沉浸时坐标为负
            if (config.immersionStatusBar)
                min(emptyHeight, config.bottomBorder - statusBarHeight(view) - view.height)
            else
                min(emptyHeight, config.bottomBorder - view.height)
        }
    }

    /**
     * 根据吸附类别，更新浮窗位置
     */
    private fun updateFloat(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager
    ) {
        initBoarderValue(view, params)
        sideAnim(view, params, windowManager)
    }

    private fun sideAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager
    ) {
        initDistanceValue(params)
        val isX: Boolean
        val end = when (config.sidePattern) {
            SidePattern.RESULT_HORIZONTAL -> {
                isX = true
                if (leftDistance < rightDistance) leftBorder else params.x + rightDistance
            }
            else -> return
        }
        val animator = ValueAnimator.ofInt(if (isX) params.x else params.y, end)
        animator.addUpdateListener {
            try {
                if (isX) params.x = it.animatedValue as Int else params.y = it.animatedValue as Int
                // 极端情况，还没吸附就调用了关闭浮窗，会导致吸附闪退
                windowManager.updateViewLayout(view, params)
            } catch (e: Exception) {
                animator.cancel()
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                dragEnd(view)
            }

            override fun onAnimationCancel(animation: Animator?) {
                dragEnd(view)
            }

            override fun onAnimationStart(animation: Animator?) {
                isAnim = true
            }
        })
        animator.start()
    }

    private fun dragEnd(view: View) {
        isAnim = false
        mQFloatCallbackWrap.onDragEnd(view)
    }

    /**
     * 计算一些边界距离数据
     */
    private fun initDistanceValue(params: WindowManager.LayoutParams) {
        leftDistance = params.x - leftBorder
        rightDistance = rightBorder - params.x
        topDistance = params.y - topBorder
        bottomDistance = bottomBorder - params.y

        minX = min(leftDistance, rightDistance)
        minY = min(topDistance, bottomDistance)
    }

    private fun statusBarHeight(view: View) = DisplayUtils.statusBarHeight(view)
    class QFloatCallbackWrap : QFloatCallback {
        val callbacks = ArrayList<QFloatCallback>()
        override fun onCreate(view: View) {
            callbacks.forEach {
                it.onCreate(view)
            }
        }

        override fun onShow(view: View) {
            callbacks.forEach {
                it.onShow(view)
            }
        }

        override fun onHide(view: View) {
            callbacks.forEach {
                it.onHide(view)
            }
        }

        override fun onDismiss() {
            callbacks.forEach {
                it.onDismiss()
            }
            callbacks.clear()
        }

        /**
         * 触摸事件的回调
         */
        override fun onTouchEvent(view: View, event: MotionEvent) {
            callbacks.forEach {
                it.onTouchEvent(view, event)
            }
        }

        /**
         * 浮窗被拖拽时的回调，坐标为浮窗的左上角坐标
         */
        override fun onDrag(view: View, event: MotionEvent) {
            callbacks.forEach {
                it.onDrag(view, event)
            }
        }

        /**
         * 拖拽结束时的回调，坐标为浮窗的左上角坐标
         */
        override fun onDragEnd(view: View) {
            callbacks.forEach {
                it.onDragEnd(view)
            }
        }
    }
}