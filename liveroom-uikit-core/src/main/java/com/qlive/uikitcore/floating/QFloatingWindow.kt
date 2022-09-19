package com.qlive.uikitcore.floating

import android.animation.Animator
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentActivity
import com.qlive.uikitcore.floating.permission.OnPermissionResult
import com.qlive.uikitcore.floating.permission.PermissionUtils
import com.qlive.uikitcore.floating.uitls.DefaultAnimator
import com.qlive.uikitcore.floating.uitls.LifecycleUtils

abstract class QFloatingWindow : QFloatCallback {

    abstract fun getLayoutID(): Int

    private var mFloatWindowManager: QFloatingWindowManager? = null
    protected var attachView: View? = null
        private set

    // 是否正在被拖拽
    val isDrag: Boolean
        get() = mFloatWindowManager?.isDrag ?: false

    // 是否正在执行动画
    val isAnim: Boolean
        get() = mFloatWindowManager?.isAnim ?: false

    // 是否显示
    val isShow: Boolean
        get() = mFloatWindowManager?.isShow ?: false

    val isCreated: Boolean
        get() = mFloatWindowManager?.isCreated ?: false

    fun getConfig(): QFloatConfig? {
        return mFloatWindowManager?.config
    }

    override fun onCreate(view: View) {
        super.onCreate(view)
        attachView = view
        LifecycleUtils.addActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    override fun onDismiss() {
        super.onDismiss()
        mFloatWindowManager = null
        attachView = null
        super.onDismiss()
        LifecycleUtils.removeActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    private var mFloatCallback: QFloatingWindowManager.QFloatCallbackWrap =
        QFloatingWindowManager.QFloatCallbackWrap()

    fun addQFloatCallback(callback: QFloatCallback) {
        mFloatCallback.callbacks.add(callback)
    }

    fun removeQFloatCallback(callback: QFloatCallback) {
        mFloatCallback.callbacks.remove(callback)
    }

    /**
     * Dismiss
     *
     * @param force
     */
    open fun dismiss(force: Boolean = true) {
        mFloatWindowManager?.dismiss(force)
        mFloatWindowManager = null
    }

    /**
     * 隐藏当前浮窗
     */
    open fun hide() {
        mFloatWindowManager?.setVisible(View.GONE)
    }

    /**
     * 设置当前浮窗可见
     */
    open fun show() {
        mFloatWindowManager?.setVisible(View.VISIBLE)
    }

    fun dragEnable(dragEnable: Boolean) {
        mFloatWindowManager?.config?.dragEnable = dragEnable
    }

    /**
     * 更新浮窗坐标、以及大小，未指定数值（全为-1）执行吸附动画；
     * 需要修改的参数，传入具体数值，不需要修改的参数保持-1即可
     * @param x         更新后的X轴坐标
     * @param y         更新后的Y轴坐标
     * @param width     更新后的宽度
     * @param height    更新后的高度
     */
    fun updateFloat(
        x: Int = -1,
        y: Int = -1,
        width: Int = -1,
        height: Int = -1
    ) {
        mFloatWindowManager?.updateFloat(x, y, width, height)
    }

    private val mActivityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {}
        override fun onActivityStarted(p0: Activity) {}
        override fun onActivityResumed(p0: Activity) {
            checkShow(p0)
        }

        override fun onActivityPaused(p0: Activity) {}

        override fun onActivityStopped(p0: Activity) {
            checkHide(p0)
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

        override fun onActivityDestroyed(p0: Activity) {}

        private fun checkHide(activity: Activity) {
            if (!activity.isFinishing && LifecycleUtils.isForeGround()) return
            // 判断浮窗是否需要关闭
            if (activity.isFinishing) mFloatWindowManager?.params?.token?.let {
                // 如果token不为空，并且是当前销毁的Activity，关闭浮窗，防止窗口泄漏
                if (it == activity.window?.decorView?.windowToken) {
                    dismiss(true)
                }
            }
            getConfig()?.apply {
                if (!LifecycleUtils.isForeGround() && showPattern != ShowPattern.CURRENT_ACTIVITY) {
                    // 当app处于后台时，全局、仅后台显示的浮窗，如果没有手动隐藏，需要显示
                    if (showPattern == ShowPattern.FOREGROUND) {
                        hide()
                    } else {
                        show()
                    }
                }
            }

        }

        private fun checkShow(activity: Activity) {
            getConfig()?.apply {
                when (// 当前页面的浮窗，不需要处理
                    showPattern) {
                    ShowPattern.CURRENT_ACTIVITY -> return@apply
                    // 仅后台显示模式下，隐藏浮窗
                    ShowPattern.BACKGROUND -> hide()
                    // 如果没有手动隐藏浮窗，需要考虑过滤信息
                    else -> show()
                }
            }
        }
    }

    internal fun attach(window: QFloatingWindowManager) {
        mFloatWindowManager = window
    }

    fun create(
        activity: FragmentActivity,
        config: QFloatConfig,
        createCall: ((Boolean, String) -> Unit)
    ) {
        config.layoutID = getLayoutID()
        val windowManager = QFloatingWindowManager(activity, config)
        windowManager.addQFloatCallback(this)
        windowManager.addQFloatCallback(mFloatCallback)

        val createAction = {
            if (windowManager.create()) {
                attach(windowManager)
                windowManager.dispatchCreate()
                createCall.invoke(true, "")
            } else {
                windowManager.clear()
            }
        }
        if (config.showPattern == ShowPattern.CURRENT_ACTIVITY) {
            createAction.invoke()
        } else if (PermissionUtils.checkPermission(activity)) {
            createAction.invoke()
        } else {
            PermissionUtils.requestPermission(activity, object : OnPermissionResult {
                override fun permissionResult(isOpen: Boolean) {
                    if (isOpen) {
                        createAction.invoke()
                    } else {
                        createCall.invoke(
                            false,
                            "No permission exception. You need to turn on overlay permissions"
                        )
                    }
                }
            })
        }
    }

}


data class QFloatConfig(
    internal var layoutID: Int = -1,
    var dragEnable: Boolean = true,
    // 状态栏沉浸
    var immersionStatusBar: Boolean = false,
    // 浮窗的吸附方式（默认不吸附，拖到哪里是哪里）
    var sidePattern: SidePattern = SidePattern.DEFAULT,

    // 浮窗显示类型（默认只在当前页显示）
    var showPattern: ShowPattern = ShowPattern.CURRENT_ACTIVITY,

    // 宽高是否充满父布局
    var widthMatch: Boolean = false,
    var heightMatch: Boolean = false,

    // 浮窗的摆放方式，使用系统的Gravity属性
    var gravity: Int = 0,
    // 坐标的偏移量
    var offsetPair: Pair<Int, Int> = Pair(0, 0),
    // 固定的初始坐标，左上角坐标
    var locationPair: Pair<Int, Int> = Pair(0, 0),
    // ps：优先使用固定坐标，若固定坐标不为原点坐标，gravity属性和offset属性无效

    // 四周边界值
    var leftBorder: Int = 0,
    var topBorder: Int = -999,
    var rightBorder: Int = 9999,
    var bottomBorder: Int = 9999,

// 出入动画
    var floatAnimator: OnFloatAnimator? = DefaultAnimator(),

// 不需要显示系统浮窗的页面集合，参数为类名
    val filterSet: MutableSet<String> = mutableSetOf(),

// 当layout大小变化后，整体view的位置的摆放
    var layoutChangedGravity: Int = Gravity.TOP.or(Gravity.START)
)

enum class ShowPattern {
    // 只在当前Activity显示、仅应用前台时显示、仅应用后台时显示，一直显示（不分前后台）
    CURRENT_ACTIVITY, FOREGROUND, BACKGROUND, ALL_TIME
}

enum class SidePattern {
    // 默认不贴边，跟随手指移动
    DEFAULT,

    // 拖拽时跟随手指移动，结束时贴边
    RESULT_HORIZONTAL
}

interface OnFloatAnimator {
    fun enterAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? = null

    fun exitAnim(
        view: View,
        params: WindowManager.LayoutParams,
        windowManager: WindowManager,
        sidePattern: SidePattern
    ): Animator? = null
}

