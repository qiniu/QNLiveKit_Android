package com.qlive.uikitpk

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import com.qlive.pkservice.QPKService
import com.qlive.core.*
import com.qlive.core.been.QInvitation
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.uikitcore.QLiveFuncComponent
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.ext.asToast


class FuncCPTPKApplyMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var isShowTip = false

    private val mPKInvitationListener = object : QInvitationHandlerListener {
        //收到邀请
        override fun onReceivedApply(pkInvitation: QInvitation) {
            kitContext ?: return
            if ((client?.getService(QLinkMicService::class.java)?.allLinker?.size ?: 0) > 1) {
                if (!isShowTip) {
                    CommonTipDialog.TipBuild()
                        .setTittle(kitContext!!.androidContext.getString(R.string.tip))
                        .setContent(
                            kitContext!!.androidContext.getString(
                                R.string.pk_auto_reject_when_link_tip,
                                pkInvitation.initiator.nick
                            )
                        )
                        .setPositiveText(kitContext!!.androidContext.getString(R.string.confirm))
                        .isNeedCancelBtn(false)
                        .build("FuncCPTPKApplyMonitor——autoreject")
                        .show(kitContext!!.fragmentManager, "")
                    isShowTip = true
                }

                client!!.getService(QPKService::class.java)
                    .invitationHandler.reject(pkInvitation.invitationID, null,
                        object :
                            QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                                // msg?.asToast()
                            }

                            override fun onSuccess(data: Void?) {
                            }
                        })
                return
            }

            if (client?.getService(QPKService::class.java)?.currentPKingSession() != null) {
                client!!.getService(QPKService::class.java)
                    .invitationHandler.reject(pkInvitation.invitationID, null,
                        object :
                            QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                                // msg?.asToast()
                            }

                            override fun onSuccess(data: Void?) {
                            }
                        })
                return
            }

            CommonTipDialog.TipBuild()
                .setTittle(kitContext!!.androidContext.getString(R.string.pk_dialog_apply_tittle))
                .setContent(
                    kitContext!!.androidContext.getString(
                        R.string.pk_dialog_apply_content,
                        pkInvitation.initiator.nick
                    )
                )
                .setNegativeText(kitContext!!.androidContext.getString(R.string.reject))
                .setPositiveText(kitContext!!.androidContext.getString(R.string.accept))
                .setListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        client!!.getService(QPKService::class.java)
                            .invitationHandler.accept(pkInvitation.invitationID, null,
                                object :
                                    QLiveCallBack<Void> {
                                    override fun onError(code: Int, msg: String?) {
                                        msg?.asToast(kitContext?.androidContext)
                                    }

                                    override fun onSuccess(data: Void?) {
                                    }
                                })
                    }

                    override fun onDialogNegativeClick(dialog: DialogFragment, any: Any) {
                        super.onDialogNegativeClick(dialog, any)
                        client!!.getService(QPKService::class.java)
                            .invitationHandler.reject(pkInvitation.invitationID, null,
                                object :
                                    QLiveCallBack<Void> {
                                    override fun onError(code: Int, msg: String?) {
                                        // msg?.asToast()
                                    }

                                    override fun onSuccess(data: Void?) {
                                    }
                                })
                    }
                }
                ).build("FuncCPTPKApplyMonitor——onReceivedApply")
                .show(kitContext!!.fragmentManager, "")
        }

        override fun onApplyCanceled(pkInvitation: QInvitation) {}

        override fun onApplyTimeOut(pkInvitation: QInvitation) {}

        override fun onAccept(pkInvitation: QInvitation) {}

        override fun onReject(pkInvitation: QInvitation) {}
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QPKService::class.java).invitationHandler
            .addInvitationHandlerListener(
                mPKInvitationListener
            )
    }

    override fun onDestroyed() {
        client?.getService(QPKService::class.java)?.invitationHandler
            ?.removeInvitationHandlerListener(
                mPKInvitationListener
            )
        super.onDestroyed()
    }

}