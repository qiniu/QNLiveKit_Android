package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.DialogFragment
import com.qlive.core.QClientType
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.pubchatservice.QPublicChatService
import com.qlive.uikit.R
import com.qlive.uikit.RoomPushActivity
import com.qlive.uikit.databinding.KitDialogCloseTypeBinding
import com.qlive.uikitcore.QKitImageView
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.isTrailering
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

//关闭房间按钮
class CloseRoomView : QKitImageView {

    enum class AnchorCloseType {
        //直接离开
        LEFT,

        //销毁房间
        CLOSE,

        //显示选择弹窗
        SHOW_SELECT
    }

    //静态配置
    companion object {
        //是否需要在离开前显示选择提示
        var anchorCloseType = AnchorCloseType.SHOW_SELECT

        /**
         * activity销毁前的通知
         * @param isAnchorActionCloseRoom 当前操作是不是主播要关闭房间 否则是离开房间
         */
        var beforeFinishCall: (QLiveUIKitContext, QLiveClient, QLiveRoomInfo, isAnchorActionCloseRoom: Boolean) -> Unit =
            { _, _, _, _ ->

            }

        /**
         * 离开动作拦截回调
         * @param resultCall 回调是否允许执行离开动作
         * @param isAnchorActionCloseRoom 当前操作是不是主播要关闭房间 否则是离开房间
         */
        var beforeCloseFilter: (QLiveUIKitContext, QLiveClient, QLiveRoomInfo, isAnchorActionCloseRoom: Boolean, resultCall: (Boolean) -> Unit) -> Unit =
            { _, _, _, _, ret ->
                //默认允许执行
                ret.invoke(true)
            }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setDoubleCheckClickListener {

            val room = this.roomInfo
            if (room == null) {
                kitContext?.currentActivity?.finish()
            }
            val call = object :
                QLiveCallBack<Void> {
                var isAnchorClose = false
                override fun onError(code: Int, msg: String?) {
                    LoadingDialog.cancelLoadingDialog()
                    msg?.asToast(context)
                    kitContext?:return
                    client?:return
                    room?:return
                    beforeFinishCall.invoke(kitContext!!, client!!, room, isAnchorClose)
                    kitContext?.currentActivity?.finish()
                }

                override fun onSuccess(data: Void?) {
                    LoadingDialog.cancelLoadingDialog()
                    kitContext?:return
                    client?:return
                    room?:return
                    beforeFinishCall.invoke(kitContext!!, client!!, room, isAnchorClose)
                    kitContext?.currentActivity?.finish()
                }
            }
            val doClose: (isAnchorClose: Boolean) -> Unit = { isAnchorClose ->
                beforeCloseFilter.invoke(kitContext!!, client!!, room!!, isAnchorClose) {
                    if (it) {
                        call.isAnchorClose = isAnchorClose
                        LoadingDialog.showLoading(kitContext!!.fragmentManager)
                        //发离开房间消息
                        client?.getService(QPublicChatService::class.java)
                            ?.sendByeBye(context.getString(R.string.live_bye_bye_tip), null)
                        //调用关闭房间方法
                        kitContext?.leftRoomActionCall?.invoke(isAnchorClose, call)
                    }
                }
            }

            if (client?.clientType == QClientType.PUSHER) {
                when (anchorCloseType) {
                    AnchorCloseType.SHOW_SELECT -> {
                        AnchorCloseTypeDialog().apply {
                            mDefaultListener = object : FinalDialogFragment.BaseDialogListener() {
                                override fun onDialogPositiveClick(
                                    dialog: DialogFragment,
                                    any: Any
                                ) {
                                    if (any == AnchorCloseType.CLOSE) {
                                        doClose.invoke(true)
                                    } else {
                                        doClose.invoke(false)
                                    }
                                }
                            }
                        }.show(kitContext!!.fragmentManager, "")
                    }
                    AnchorCloseType.CLOSE -> {
                        doClose.invoke(true)
                    }
                    AnchorCloseType.LEFT -> {
                        doClose.invoke(false)
                    }
                }
            } else {
                doClose.invoke(false)
            }
        }
    }

    override fun attachKitContext(context: QLiveUIKitContext) {
        super.attachKitContext(context)
        visibility = View.GONE
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        super.onGetLiveRoomInfo(roomInfo)
        val roomId = kitContext!!.currentActivity.intent.getStringExtra(RoomPushActivity.KEY_ROOM_ID) ?: ""
        if (roomInfo.isTrailering() && client?.clientType == QClientType.PUSHER && roomId.isNotEmpty()) {
            visibility = VISIBLE
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        visibility = VISIBLE
        if (!isResumeUIFromFloating) {
            //发进入房间消息
            client?.getService(QPublicChatService::class.java)
                ?.sendWelCome(context.getString(R.string.live_welcome_tip), null)
        }
    }

    class AnchorCloseTypeDialog : ViewBindingDialogFragment<KitDialogCloseTypeBinding>() {
        override fun init() {
            binding.btnClose.setDoubleCheckClickListener {
                mDefaultListener?.onDialogPositiveClick(this, AnchorCloseType.CLOSE)
                dismiss()
            }
            binding.btnLeft.setDoubleCheckClickListener {
                mDefaultListener?.onDialogPositiveClick(this, AnchorCloseType.LEFT)
                dismiss()
            }
            binding.btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

}
