package com.qlive.uikitcore

import androidx.lifecycle.*
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.liblog.QLiveLogUtil


interface BaseComponent<T : BaseContext> {
    var kitContext: T?
    fun attachKitContext(context: T) {
        this.kitContext = context
        val owner = context.lifecycleOwner
        owner.lifecycle.addObserver(object : LifecycleObserver {
            //使用这种方式的原因 个别客户不愿意用Lifecycle 2.1.0 以上
            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun onCreated() {
                onStateChanged(owner, Lifecycle.Event.ON_CREATE)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                onStateChanged(owner, Lifecycle.Event.ON_START)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                onStateChanged(owner, Lifecycle.Event.ON_RESUME)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                onStateChanged(owner, Lifecycle.Event.ON_PAUSE)
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                onStateChanged(owner, Lifecycle.Event.ON_STOP)
            }
        })
    }

    /**
     * activity 生命周期
     */
    open fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
       // QLiveLogUtil.d("onStateChanged onStateChangedonStateChangedonStateChanged ${event.name}")
        if (event == Lifecycle.Event.ON_DESTROY) {
            kitContext = null
        }
    }

    /**
     * 注册UI组件之间的通信事件
     * @param clz 事件类
     * @param call 回调函数
     * @param <T>
    </T> */
    fun <T : UIEvent?> registerEventAction(clz: Class<T>, call: Function1<T, Unit>) {
        kitContext?.eventManager?.mActionMap?.get(this)?.let {
            JavaGenericsEliminateUtil.registerEventAction(it, clz, call)
        }
    }

    /**
     * 发送UI通信事件
     * 该event 将会被发送到所有注册关心该事件的UI组件中去
     * @param event 事件对象
     * @param <T>
    </T> */
    fun <T : UIEvent?> sendUIEvent(event: T) {
        kitContext?.eventManager?.sendUIEvent(event)
    }
}

/**
 * 小组件
 */
interface QComponent : BaseComponent<QUIKitContext> {
    override var kitContext: QUIKitContext?
}


/**
 * 直播间内小组件
 *
 * 父接口：
 * QClientLifeCycleListener -> client 生命周期
 * LifecycleEventObserver ->  房间客户端生命周期
 */
interface QLiveComponent : QLiveUILifeCycleListener, BaseComponent<QLiveUIKitContext> {

    var client: QLiveClient?
    var roomInfo: QLiveRoomInfo?
    var user: QLiveUser?
    override var kitContext: QLiveUIKitContext?

    /**
     * 绑定房间客户端回调
     * @param client 组件拿到client 就能拿到所有访问业务服务的能力 如发消息 设置监听
     */
    fun attachLiveClient(client: QLiveClient) {
        this.client = client
    }

    /**
     * 房间加入成功回调
     * @param roomInfo 加入哪个房间
     */
    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        this.roomInfo = roomInfo
    }

    /**
     * 房间正在进入回调
     */
    override fun onEntering(roomId: String, user: QLiveUser) {
        this.user = user
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {

    }

    /**
     * 当前房间已经离开回调 - 我是观众-离开 我是主播对应关闭房间
     */
    override fun onLeft() {

    }

    /**
     * client销毁回调 == 房间页面将要退出
     */
    override fun onDestroyed() {
        client = null
    }

}

/**
 * 依赖房间的子页面小组件，能获取到从哪个房间跳转过来无法获取到client
 * @constructor Create empty Q room depends component
 */
interface QRoomComponent : BaseComponent<QLiveUIKitContext> {

    var roomInfo: QLiveRoomInfo?
    var user: QLiveUser?
    var client: QLiveClient?

    /**
     * 绑定房间客户端回调
     * @param client 组件拿到client 就能拿到所有访问业务服务的能力 如发消息 设置监听
     */
    fun attachLiveClient(client: QLiveClientClone) {
        this.client = client
    }

    /**
     * On entering
     *
     * @param roomInfo
     * @param user
     */
    fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        this.roomInfo = roomInfo
        this.user = user
    }
}




