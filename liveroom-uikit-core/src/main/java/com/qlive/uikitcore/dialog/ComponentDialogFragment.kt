package com.qlive.uikitcore.dialog

import android.os.Bundle
import android.view.View
import com.qlive.roomservice.QRoomService
import com.qlive.uikitcore.QLiveComponentManagerOwner
import com.qlive.uikitcore.QLiveUIKitContext

abstract class ComponentDialogFragment(
    open val kitContext: QLiveUIKitContext,
) : FinalDialogFragment() {

    private val mLiveComponentManager by lazy {
        (activity as QLiveComponentManagerOwner).getQLiveComponentManager()
    }
    private var kitContextWrap: QLiveUIKitContext? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kitContextWrap =  QLiveUIKitContext(
            requireContext(),
            childFragmentManager,
            requireActivity(),
            this,
            kitContext.leftRoomActionCall,
            kitContext.startPusherRoomActionCall,
            kitContext.getPlayerRenderViewCall,
            kitContext.getPusherRenderViewCall
        )
        mLiveComponentManager.scanComponent(view, kitContextWrap!!)
        if (mLiveComponentManager.liveId.isNotEmpty()) {
            mLiveComponentManager.onEntering(
                mLiveComponentManager.liveId,
                mLiveComponentManager.user!!,
                kitContextWrap!!
            )
        }
        mLiveComponentManager.roomInfo?.let {
            mLiveComponentManager.onGetLiveRoomInfo(it, kitContextWrap!!)
        }
        mLiveComponentManager.roomClient.getService(QRoomService::class.java)?.roomInfo?.let {
            mLiveComponentManager.onJoined(it, true, kitContextWrap!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        kitContextWrap?.destroyContext()
    }
}
