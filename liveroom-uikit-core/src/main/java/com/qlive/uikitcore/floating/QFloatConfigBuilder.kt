package com.qlive.uikitcore.floating

class QFloatConfigBuilder {
    // 创建浮窗数据类，方便管理配置
    private var config = QFloatConfig()

    /**
     * 设置浮窗的吸附模式
     * @param sidePattern   浮窗吸附模式
     */
    fun setSidePattern(sidePattern: SidePattern) = apply { config.sidePattern = sidePattern }

    /**
     * 设置浮窗的显示模式
     * @param showPattern   浮窗显示模式
     */
    fun setShowPattern(showPattern: ShowPattern) = apply { config.showPattern = showPattern }


    /**
     * 设置浮窗的对齐方式，以及偏移量
     * @param gravity   对齐方式
     * @param offsetX   目标坐标的水平偏移量
     * @param offsetY   目标坐标的竖直偏移量
     */
    @JvmOverloads
    fun setGravity(gravity: Int, offsetX: Int = 0, offsetY: Int = 0) = apply {
        config.gravity = gravity
        config.offsetPair = Pair(offsetX, offsetY)
    }

    /**
     * 当layout大小变化后，整体view的位置的对齐方式
     * 比如，当设置为 Gravity.END 时，当view的宽度变小或者变大时，都将会以原有的右边对齐 <br/>
     * 默认对齐方式为左上角
     * @param gravity   对齐方式
     */
    fun setLayoutChangedGravity(gravity: Int) = apply {
        config.layoutChangedGravity = gravity;
    }

    /**
     * 设置浮窗的起始坐标，优先级高于setGravity
     * @param x     起始水平坐标
     * @param y     起始竖直坐标
     */
    fun setLocation(x: Int, y: Int) = apply { config.locationPair = Pair(x, y) }

    /**
     * 设置浮窗的拖拽边距值
     * @param left      浮窗左侧边距
     * @param top       浮窗顶部边距
     * @param right     浮窗右侧边距
     * @param bottom    浮窗底部边距
     */
    @JvmOverloads
    fun setBorder(
        left: Int = 0,
        right: Int = 0,
        top: Int = 0,
        bottom: Int = 0
    ) = apply {
        config.leftBorder = left
        config.topBorder = top
        config.rightBorder = right
        config.bottomBorder = bottom
    }


    /**
     * 设置浮窗是否可拖拽
     * @param dragEnable    是否可拖拽
     */
    fun setDragEnable(dragEnable: Boolean) = apply { config.dragEnable = dragEnable }

    /**
     * 设置浮窗是否状态栏沉浸
     * @param immersionStatusBar    是否状态栏沉浸
     */
    fun setImmersionStatusBar(immersionStatusBar: Boolean) =
        apply { config.immersionStatusBar = immersionStatusBar }


    /**
     * 设置浮窗的出入动画
     * @param floatAnimator     浮窗的出入动画，为空时不执行动画
     */
    fun setAnimator(floatAnimator: OnFloatAnimator?) =
        apply { config.floatAnimator = floatAnimator }


    /**
     * 设置浮窗宽高是否充满屏幕
     * @param widthMatch    宽度是否充满屏幕
     * @param heightMatch   高度是否充满屏幕
     */
    fun setMatchParent(widthMatch: Boolean = false, heightMatch: Boolean = false) = apply {
        config.widthMatch = widthMatch
        config.heightMatch = heightMatch
    }

    /**
     * 设置需要过滤的Activity类名，仅对系统浮窗有效
     * @param clazz     需要过滤的Activity类名
     */
    fun setFilter( vararg clazz: Class<*>) = apply {
        clazz.forEach {
            config.filterSet.add(it.name)
        }
    }

    fun build(): QFloatConfig {
        return config
    }

}