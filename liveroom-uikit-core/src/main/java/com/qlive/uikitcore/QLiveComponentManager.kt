package com.qlive.uikitcore

import android.view.View
import android.view.ViewGroup
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.liblog.QLiveLogUtil

class QLiveComponentManager(
    private val roomClient: QLiveClient,
    private val kitContext: QLiveUIKitContext
) : QLiveUILifeCycleListener {

    val mComponents = HashSet<QLiveComponent>()

    init {
        kitContext.mAllComponentCall = {
            mComponents.toList()
        }
    }

    fun addFuncComponent(funcComponent: QLiveFuncComponent) {
        mComponents.add(funcComponent)
        funcComponent.attachKitContext(kitContext)
        funcComponent.attachLiveClient(roomClient)
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
                if(child==null){
                    QLiveLogUtil.d("scanComponent", "scanComponentInner null "+view.javaClass.canonicalName)
                }
                scanComponentInner(child)
            }
        }
    }

    private fun checkAttachView(view: View) {
        if (view is QLiveComponent) {
            QLiveLogUtil.d("scanComponent", "checkAttachView "+view.javaClass.canonicalName)
            mComponents.add(view)
        }
    }

    override fun onEntering(liveId: String, user: QLiveUser) {
        mComponents.forEach {
            it.onEntering(liveId, user)
        }
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        mComponents.forEach {
            it.onGetLiveRoomInfo(roomInfo)
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        mComponents.forEach {
            it.onJoined(roomInfo, isResumeUIFromFloating)
        }
    }

    override fun onLeft() {
        mComponents.forEach {
            it.onLeft()
        }
    }

    override fun onDestroyed() {
        kitContext.eventManager.clear()
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
        kitContext.mAllComponentCall = {
            mComponents.toList()
        }
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
                if(child==null){
                    QLiveLogUtil.d("scanComponent", "scanComponentInner null "+view.javaClass.canonicalName)
                }
                scanComponentInner(child)
            }
        }
    }

    private fun checkAttachView(view: View) {
        if (view is QRoomComponent) {
            QLiveLogUtil.d("scanComponent", "checkAttachView "+view.javaClass.canonicalName)
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
        kitContext.mAllComponentCall = {
            mComponents.toList()
        }
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
