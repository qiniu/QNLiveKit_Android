package com.qlive.uikitcore

import android.view.View
import android.view.ViewGroup
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.liblog.QLiveLogUtil

interface QLiveComponentManagerOwner {
    fun getQLiveComponentManager(): QLiveComponentManager
}

class QLiveComponentManager(
    val roomClient: QLiveClient
) : QLiveUILifeCycleListener {

    val mComponents = HashSet<QLiveComponent>()
    var roomInfo: QLiveRoomInfo? = null
    var user: QLiveUser? = null
    var liveId = ""

    fun addFuncComponent(funcComponent: QLiveFuncComponent, kitContext: QLiveUIKitContext) {
        mComponents.add(funcComponent)
        funcComponent.attachKitContext(kitContext)
        funcComponent.attachLiveClient(roomClient)
    }

    fun scanComponent(view: View, kitContext: QLiveUIKitContext) {
        kitContext.sharedAllComponent = mComponents
        val componentList = ArrayList<QLiveComponent>()
        scanComponentInner(view, componentList)
        mComponents.addAll(componentList)
        componentList.forEach {
            it.attachKitContext(kitContext)
            it.attachLiveClient(roomClient)
        }
    }

    private fun scanComponentInner(view: View, componentList: ArrayList<QLiveComponent>) {
        checkAttachView(view,componentList)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                scanComponentInner(child, componentList)
            }
        }
    }

    private fun checkAttachView(view: View, componentList: ArrayList<QLiveComponent>) {
        if (view is QLiveComponent) {
            QLiveLogUtil.d(
                "QLiveComponentManager",
                "checkAttachView " + view.javaClass.canonicalName
            )
            componentList.add(view)
        }
    }

    override fun onEntering(liveId: String, user: QLiveUser) {
        this.liveId = liveId
        this.user = user
        mComponents.forEach {
            it.onEntering(liveId, user)
        }
    }

    fun onEntering(liveId: String, user: QLiveUser, kitContext: QLiveUIKitContext) {
        this.liveId = liveId
        this.user = user
        mComponents.forEach {
            if (it.kitContext == kitContext) {
                it.onEntering(liveId, user)
            }
        }
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        this.roomInfo = roomInfo
        mComponents.forEach {
            it.onGetLiveRoomInfo(roomInfo)
        }
    }

    fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo, kitContext: QLiveUIKitContext) {
        this.roomInfo = roomInfo
        mComponents.forEach {
            if (it.kitContext == kitContext) {
                it.onGetLiveRoomInfo(roomInfo)
            }
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        mComponents.forEach {
            it.onJoined(roomInfo, isJoinedBefore)
        }
    }

    fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean, kitContext: QLiveUIKitContext) {
        mComponents.forEach {
            if (it.kitContext == kitContext) {
                it.onJoined(roomInfo, isJoinedBefore)
            }
        }
    }

    override fun onLeft() {
        mComponents.forEach {
            it.onLeft()
        }
    }

    override fun onDestroyed() {
        mComponents.forEach {
            it.onDestroyed()
        }
        mComponents.clear()
    }
}

class QLiveRoomDepdComponentManager(
    private val roomClient: QLiveClientClone,
    private val kitContext: QLiveUIKitContext
) {

    val mComponents = HashSet<QRoomComponent>()

    init {
        kitContext.sharedAllComponent = mComponents
    }

    fun scanComponent(view: View) {
        scanComponentInner(view)
        mComponents.forEach {
            it.attachKitContext(kitContext)
            it.attachLiveClient(roomClient)
        }
    }

    private fun scanComponentInner(view: View) {
        checkAttachView(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child == null) {
                    QLiveLogUtil.d(
                        "scanComponent",
                        "scanComponentInner null " + view.javaClass.canonicalName
                    )
                }
                scanComponentInner(child)
            }
        }
    }

    private fun checkAttachView(view: View) {
        if (view is QRoomComponent) {
            QLiveLogUtil.d("scanComponent", "checkAttachView " + view.javaClass.canonicalName)
            mComponents.add(view)
        }
    }

    fun onEntering(roomInfo: QLiveRoomInfo, user: QLiveUser) {
        mComponents.forEach {
            it.onEntering(roomInfo, user)
        }
    }

    fun onDestroyed() {
        kitContext.eventManager.clear()
        mComponents.clear()
    }
}

class QLiveOutRoomComponentManager(private val kitContext: QUIKitContext) {
    private val mComponents = HashSet<QComponent>()

    init {
        kitContext.sharedAllComponent = mComponents
    }

    fun scanComponent(view: View) {
        checkAttachView(view)
        if (view is ViewGroup) {
            val viewGroup = view
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                scanComponent(child)
            }
        }
    }

    private fun checkAttachView(view: View) {
        if (view is QComponent) {
            mComponents.add(view)
            (view as QComponent).attachKitContext(kitContext)
        }
    }

    fun onDestroyed() {
        kitContext.eventManager.clear()
        mComponents.clear()
    }
}
