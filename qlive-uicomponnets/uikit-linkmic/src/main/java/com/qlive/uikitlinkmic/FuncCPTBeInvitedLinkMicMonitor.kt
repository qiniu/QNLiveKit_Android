package com.qlive.uikitlinkmic

import android.Manifest
import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.qlive.avparam.QCameraParam
import com.qlive.avparam.QMicrophoneParam
import com.qlive.core.QInvitationHandlerListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QInvitation
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.uikitcore.QLiveFuncComponent
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.permission.PermissionAnywhere

/**
 * 用户被邀请连麦申请弹窗
 */
class FuncCPTBeInvitedLinkMicMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mInvitationListener = object : QInvitationHandlerListener {
        override fun onReceivedApply(qInvitation: QInvitation) {
            if (user?.userId == roomInfo?.anchor?.userId) {
                return
            }
            kitContext ?: return
            CommonTipDialog.TipBuild()
                .setTittle(
                    kitContext!!.androidContext.getString(R.string.link_dialog_apply_tittle)
                )
                .setContent(
                    kitContext!!.androidContext.getString(
                        R.string.link_dialog_apply_content,
                        qInvitation.initiator.nick
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
                                        startLink()
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
                }
                ).build("FuncCPTBeInvitedLinkMicMonitor")
                .show(kitContext!!.fragmentManager, "")
        }

        override fun onApplyCanceled(qInvitation: QInvitation) {}
        override fun onApplyTimeOut(qInvitation: QInvitation) {}
        override fun onAccept(qInvitation: QInvitation) {}
        override fun onReject(qInvitation: QInvitation) {}
    }

    private fun startLink() {
        PermissionAnywhere.requestPermission(
            kitContext!!.currentActivity as AppCompatActivity,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        ) { grantedPermissions, _, _ ->
            if (grantedPermissions.size == 2) {
                client?.getService(QLinkMicService::class.java)
                    ?.audienceMicHandler
                    ?.startLink(
                        null,
                        QCameraParam(),
                        QMicrophoneParam(),
                        object : QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                                msg?.asToast(kitContext!!.androidContext)
                            }

                            override fun onSuccess(data: Void?) {
                                MyLinkerInfoDialog.StartLinkStore.isVideoLink = true
                                MyLinkerInfoDialog.StartLinkStore.startTime =
                                    System.currentTimeMillis()
                            }
                        }
                    )
            } else {
                Toast.makeText(
                    kitContext!!.androidContext,
                    R.string.live_permission_check_tip,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
    }
}