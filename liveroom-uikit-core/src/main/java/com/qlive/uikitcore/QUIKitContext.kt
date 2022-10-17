package com.qlive.uikitcore

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.qlive.avparam.QPlayerRenderView
import com.qlive.avparam.QPushRenderView
import com.qlive.core.*
import com.qlive.core.been.QCreateRoomParam
import com.qlive.liblog.QLiveLogUtil
import com.qlive.uikitcore.ext.asToast
import java.util.*

open class BaseContext(
    /**
     * 安卓上下文
     */
    val androidContext: Context,
    /**
     * 安卓FragmentManager 用于显示弹窗
     */
    val fragmentManager: FragmentManager,
    /**
     * 当前所在的Activity
     */
    val currentActivity: Activity,
    /**
     * 当前页面的安卓LifecycleOwner
     */
    val lifecycleOwner: LifecycleOwner,
) {
    internal var mAllComponentCall: () -> List<BaseComponent<*>> = {
        LinkedList<BaseComponent<*>>()
    }

    internal val eventManager = QLiveUIEventManager().apply {
        mQLiveComponents = {
            mAllComponentCall.invoke()
        }
    }

    fun getIntent(): Intent {
        return currentActivity.intent
    }

    fun toast(resId: Int) {
        androidContext.getString(resId).asToast(androidContext)
    }

    fun toast(resId: Int, vararg formatArgs: Any?) {
        androidContext.getString(resId, formatArgs).asToast(androidContext)
    }

}

/**
 * uikit UI组件上下文
 * 1在UI组件中能获取平台特性的能力 如activiy 显示弹窗
 *
 */
class QUIKitContext(
    /**
     * 安卓上下文
     */
    androidContext: Context,
    /**
     * 安卓FragmentManager 用于显示弹窗
     */
    fragmentManager: FragmentManager,
    /**
     * 当前所在的Activity
     */
    currentActivity: Activity,
    /**
     * 当前页面的安卓LifecycleOwner
     */
    lifecycleOwner: LifecycleOwner,
) : BaseContext(androidContext, fragmentManager, currentActivity, lifecycleOwner) {
}

/**
 * uikit 房间里的UI组件上下文
 * 1在UI组件中能获取平台特性的能力 如activiy 显示弹窗
 * 2能获取房间client 主要资源和关键操作
 */
class QLiveUIKitContext(
    /**
     * 安卓上下文
     */
    androidContext: Context,
    /**
     * 安卓FragmentManager 用于显示弹窗
     */
    fragmentManager: FragmentManager,
    /**
     * 当前所在的Activity
     */
    currentActivity: Activity,
    /**
     * 当前页面的安卓LifecycleOwner
     */
    lifecycleOwner: LifecycleOwner,
    /**
     * 离开房间操作 在任意UI组件中可以操作离开房间
     * @param isAnchorClose 是不是主播关闭房间
     */
    val leftRoomActionCall: (isAnchorClose: Boolean, resultCall: QLiveCallBack<Void>) -> Unit, //离开房间操作
    /**
     * 创建并且加入房间操作 在任意UI组件中可创建并且加入房间
     * @param param 创建房间参数，非空则是创建并开始，空则代表 开始已经存在的房间
     */
    val startPusherRoomActionCall: (param: QCreateRoomParam?, resultCall: QLiveCallBack<Void>) -> Unit,//创建并加入操作
    /**
     * 获取当前播放器预览窗口 在任意UI组件中如果要对预览窗口变化可直接获取
     * 比如连麦pk组件需要改变预览窗口
     */
    val getPlayerRenderViewCall: () -> QPlayerRenderView?, //获取当前播放器预览窗口
    /**
     * 获取推流预览窗口  在任意UI组件中如果要对预览窗口变化可直接获取
     * 比如连麦pk组件需要改变预览窗口
     */
    val getPusherRenderViewCall: () -> QPushRenderView?,  //获取推流预览窗口

) : BaseContext(androidContext, fragmentManager, currentActivity, lifecycleOwner) {

    /**
     * 获得某个功能组件的对象
     *
     * @param T
     * @param serviceClass
     * @return
     */
    fun <T : QLiveFuncComponent> getLiveFuncComponent(serviceClass: Class<T>): T? {
        return JavaGenericsEliminateUtil.getLiveComponent(
            mAllComponentCall.invoke(),
            serviceClass
        )
    }
}

class QLiveClientClone(private val client: QLiveClient) : QLiveClient {
    override fun <T : QLiveService> getService(serviceClass: Class<T>): T {
        return client.getService(serviceClass)
    }

    override fun addLiveStatusListener(liveStatusListener: QLiveStatusListener) {
        client.addLiveStatusListener(liveStatusListener)
    }

    override fun removeLiveStatusListener(liveStatusListener: QLiveStatusListener) {
        client.removeLiveStatusListener(liveStatusListener)
    }

    override fun destroy() {
        QLiveLogUtil.d("QLiveClientClone can not destroy")
    }

    override fun getClientType(): QClientType {
        return client.clientType
    }
}


