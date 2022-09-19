package com.qlive.uikitcore.floating.uitls

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

object LifecycleUtils {

    lateinit var application: Application
    private var activityCount = 0
    private var mTopActivity: WeakReference<Activity>? = null
    fun getTopActivity(): Activity? = mTopActivity?.get()
    private val mActivityLifecycleCallbacksWrap = ActivityLifecycleCallbacksWrap()
    fun addActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        mActivityLifecycleCallbacksWrap.mActivityLifecycleCallbacks.add(callbacks)
    }

    fun removeActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
        mActivityLifecycleCallbacksWrap.mActivityLifecycleCallbacks.remove(callbacks)
    }

    internal fun setLifecycleCallbacks(application: Application) {
        this.application = application
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacksWrap)
        mActivityLifecycleCallbacksWrap.mActivityLifecycleCallbacks.add(object :
            Application.ActivityLifecycleCallbacks {

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                // 计算启动的activity数目
                activity.let { activityCount++ }
            }

            override fun onActivityResumed(activity: Activity) {
                activity.let {
                    mTopActivity?.clear()
                    mTopActivity = WeakReference<Activity>(it)
                    // 每次都要判断当前页面是否需要显示
                }
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                activity.let {
                    // 计算关闭的activity数目，并判断当前App是否处于后台
                    activityCount--
                }
            }

            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    fun isForeGround() = activityCount > 0

    @SuppressLint("MissingPermission")
    fun moveTaskToFront(taskId: Int, call: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            var repeatCount = -1
            while (!isForeGround() && repeatCount < 10) {
                val am: ActivityManager =
                    application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME)
                delay(500)
                repeatCount++
            }
            call.invoke()
        }
    }


    private class ActivityLifecycleCallbacksWrap : Application.ActivityLifecycleCallbacks {
        val mActivityLifecycleCallbacks = ArrayList<Application.ActivityLifecycleCallbacks>()
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivityCreated(p0, p1)
            }
        }

        override fun onActivityStarted(p0: Activity) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivityStarted(p0)
            }
        }

        override fun onActivityResumed(p0: Activity) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivityResumed(p0)
            }
        }

        override fun onActivityPaused(p0: Activity) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivityPaused(p0)
            }
        }

        override fun onActivityStopped(p0: Activity) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivityStopped(p0)
            }
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivitySaveInstanceState(p0, p1)
            }
        }

        override fun onActivityDestroyed(p0: Activity) {
            mActivityLifecycleCallbacks.forEach {
                it.onActivityDestroyed(p0)
            }
        }
    }
}