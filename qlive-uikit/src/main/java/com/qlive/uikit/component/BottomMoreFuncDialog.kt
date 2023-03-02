package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.qlive.avparam.QBeautySetting
import com.qlive.avparam.QCameraFace
import com.qlive.core.QClientType
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.pushclient.QPusherClient
import com.qlive.rtclive.RTCRenderView
import com.qlive.sdk.QLive
import com.qlive.uikit.R
import com.qlive.uikit.databinding.KitDialogAnchorMoreFuncBinding
import com.qlive.uikit.databinding.KitDialogPlayerMoreFuncBinding
import com.qlive.uikitcore.BeautyHook
import com.qlive.uikitcore.QKitImageView
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.TestUIEvent
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitgift.GiftDialog
import com.qlive.uikitlinkmic.StartLinkHandler

class AnchorBottomMoreFuncDialog(
    private val kitContext: QLiveUIKitContext,
    private val client: QLiveClient
) : ViewBindingDialogFragment<KitDialogAnchorMoreFuncBinding>() {

    init {
        applyDimAmount(0f)
        applyGravityStyle(Gravity.BOTTOM)
    }

    override fun init() {
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.llSwitch.setOnClickListener {
            (client as QPusherClient).switchCamera(object : QLiveCallBack<QCameraFace> {
                override fun onError(code: Int, msg: String?) {}
                override fun onSuccess(data: QCameraFace?) {}
            })
        }

        val llMuteSelect = (client as QPusherClient).isMicrophoneMute
        binding.llMute.isSelected = llMuteSelect
        if (llMuteSelect) {
            binding.tvMute.text =
                requireActivity().getText(R.string.live_dialog_morefunc_micphone_off)
        } else {
            binding.tvMute.text =
                requireActivity().getText(R.string.live_dialog_morefunc_micphone_on)
        }
        binding.llMute.setOnClickListener {
            val isSelect = !(client as QPusherClient).isMicrophoneMute
            (client as QPusherClient).muteMicrophone(isSelect, object : QLiveCallBack<Boolean> {
                override fun onError(code: Int, msg: String?) {
                }

                override fun onSuccess(b: Boolean) {
                    if (!b) {
                        return
                    }
                    it.isSelected = isSelect
                    if (isSelect) {
                        binding.tvMute.text =
                            requireActivity().getText(R.string.live_dialog_morefunc_micphone_off)
                    } else {
                        binding.tvMute.text =
                            requireActivity().getText(R.string.live_dialog_morefunc_micphone_on)
                    }
                }
            })
        }
        binding.llMirror.setOnClickListener {
            val isSelect = !it.isSelected
            (kitContext.getPusherRenderViewCall.invoke() as RTCRenderView?)?.getQNRender()
                ?.setMirror(isSelect)
            it.isSelected = isSelect
        }

        var isOpen = false
        if (BeautyHook.isEnable) {
            binding.llBeauty.setDoubleCheckClickListener {
                kitContext.getLiveFuncComponent(FuncCPTBeautyDialogShower::class.java)
                    ?.showBeautyEffectDialog()
            }
        } else {
            binding.llBeauty.setOnClickListener {
                if (!isOpen) {
                    isOpen = true
                    (client as QPusherClient).setDefaultBeauty(
                        QBeautySetting(
                            0.6f,
                            0.8f,
                            0.6f
                        ).apply { setEnable(true) })
                } else {
                    isOpen = false
                    (client as QPusherClient).setDefaultBeauty(
                        QBeautySetting(
                            0f,
                            0f,
                            0f
                        ).apply { setEnable(false) })
                }
            }
        }

        if (BeautyHook.isEnable) {
            binding.llStick.setDoubleCheckClickListener {
                kitContext.getLiveFuncComponent(FuncCPTBeautyDialogShower::class.java)
                    ?.showBeautyStickDialog()
            }
            binding.llStick.visibility = View.VISIBLE
        } else {
            binding.llStick.visibility = View.GONE
        }
    }
}

class PlayerBottomMoreFuncDialog(
    private val kitContext: QLiveUIKitContext,
    private val client: QLiveClient, var roomInfo: QLiveRoomInfo
) : ViewBindingDialogFragment<KitDialogPlayerMoreFuncBinding>() {

    init {
        applyDimAmount(0f)
        applyGravityStyle(Gravity.BOTTOM)
    }

    private val mGiftDialog by lazy { GiftDialog(client) }

    private var mStartLinkHandler: StartLinkHandler? = null

    override fun init() {
        if (mStartLinkHandler == null) {
            mStartLinkHandler = StartLinkHandler(requireContext()).apply {
                attachClient(client, kitContext)
                user = QLive.getLoginUser()
            }
        }
        mStartLinkHandler?.roomInfo = roomInfo
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        binding.llGift.setOnClickListener {
            mGiftDialog.show(kitContext.fragmentManager, "")
        }

        mStartLinkHandler?.attachView(binding.llLink)
    }

    fun release() {
        mStartLinkHandler?.release()
    }
}


class BottomMoreFuncButton : QKitImageView {

    private var mAnchorBottomMoreFuncDialog: AnchorBottomMoreFuncDialog? = null
    private var mPlayerBottomMoreFuncDialog: PlayerBottomMoreFuncDialog? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {
            client ?: return@setDoubleCheckClickListener
            kitContext ?: return@setDoubleCheckClickListener
            roomInfo ?: return@setDoubleCheckClickListener
            if (client?.clientType == QClientType.PLAYER) {
                if (mPlayerBottomMoreFuncDialog == null) {
                    mPlayerBottomMoreFuncDialog =
                        PlayerBottomMoreFuncDialog(kitContext!!, client!!, roomInfo!!)
                }
                mPlayerBottomMoreFuncDialog?.roomInfo = roomInfo!!
                mPlayerBottomMoreFuncDialog?.show(kitContext!!.fragmentManager, "")
            } else {
                if (mAnchorBottomMoreFuncDialog == null) {
                    mAnchorBottomMoreFuncDialog = AnchorBottomMoreFuncDialog(kitContext!!, client!!)
                }
                mAnchorBottomMoreFuncDialog?.show(kitContext!!.fragmentManager, "")
            }
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        if (client?.clientType == QClientType.PLAYER) {
            mPlayerBottomMoreFuncDialog?.roomInfo = roomInfo
        }
    }

    override fun onDestroyed() {
        if (client?.clientType == QClientType.PLAYER) {
            mPlayerBottomMoreFuncDialog?.release()
        }
        super.onDestroyed()
    }
}
