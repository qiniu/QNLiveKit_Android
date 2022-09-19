package com.qlive.uikitlinkmic

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.qlive.core.been.QInvitation
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.core.QInvitationHandlerListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.uikitcore.QLiveFuncComponent
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.FinalDialogFragment

/**
 * 主播监听连麦申请
 * 展示连麦邀请弹窗
 */
class FuncCPTLinkMicApplyMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mApplyingDialogs = HashMap<Int, DialogFragment>()

    private val mInvitationListener = object : QInvitationHandlerListener {
        override fun onReceivedApply(qInvitation: QInvitation) {
            if (user?.userId != roomInfo?.anchor?.userId) {
                return
            }
            CommonTipDialog.TipBuild()
                .setTittle(kitContext!!.androidContext.getString(R.string.link_dialog_apply_tittle))
                .setContent(
                    kitContext!!.androidContext?.getString(
                        R.string.link_dialog_apply_content,
                        " ${qInvitation.initiator.nick} "
                    )
                )
                .setNegativeText(kitContext!!.androidContext.getString(R.string.reject))
                .setPositiveText(kitContext!!.androidContext.getString(R.string.accept))
                .setListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        client!!.getService(QLinkMicService::class.java)
                            .invitationHandler.accept(qInvitation.invitationID, null,
                                object :
                                    QLiveCallBack<Void> {
                                    override fun onError(code: Int, msg: String?) {
                                        Toast.makeText(
                                            kitContext!!.androidContext,
                                            msg,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onSuccess(data: Void?) {
                                    }
                                })
                    }

                    override fun onDialogNegativeClick(dialog: DialogFragment, any: Any) {
                        super.onDialogNegativeClick(dialog, any)
                        client!!.getService(QLinkMicService::class.java)
                            .invitationHandler.reject(qInvitation.invitationID, null,
                                object :
                                    QLiveCallBack<Void> {
                                    override fun onError(code: Int, msg: String?) {
                                        // msg?.asToast()
                                    }

                                    override fun onSuccess(data: Void?) {
                                    }
                                })
                    }

                    override fun onDismiss(dialog: DialogFragment) {
                        super.onDismiss(dialog)
                        mApplyingDialogs.remove(qInvitation.invitationID)
                    }
                }
                ).build("FuncCPTLinkMicApplyMonitor").apply {
                    mApplyingDialogs.put(qInvitation.invitationID, this)
                }
                .show(kitContext!!.fragmentManager, "")
        }

        override fun onApplyCanceled(qInvitation: QInvitation) {
            mApplyingDialogs.get(qInvitation.invitationID)?.dismiss()
            mApplyingDialogs.remove(qInvitation.invitationID)
        }

        override fun onApplyTimeOut(qInvitation: QInvitation) {
            mApplyingDialogs.get(qInvitation.invitationID)?.dismiss()
            mApplyingDialogs.remove(qInvitation.invitationID)
        }

        override fun onAccept(qInvitation: QInvitation) {}
        override fun onReject(qInvitation: QInvitation) {}
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QLinkMicService::class.java)?.invitationHandler?.addInvitationHandlerListener(
            mInvitationListener
        )
    }

    override fun onDestroyed() {
        client?.getService(QLinkMicService::class.java)?.invitationHandler?.removeInvitationHandlerListener(
            mInvitationListener
        )
        super.onDestroyed()
        mApplyingDialogs.clear()
    }
}