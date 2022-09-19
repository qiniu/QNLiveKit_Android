package com.qlive.uikitlinkmic

import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.DialogFragment
import com.qlive.avparam.QCameraParam
import com.qlive.avparam.QMicrophoneParam
import com.qlive.core.been.QInvitation
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.core.QInvitationHandlerListener
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.pkservice.QPKService
import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.uikitcore.QKitImageView
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.ext.asToast

//开始连麦按钮
class StartLinkView : QKitImageView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    //连麦邀请监听
    private val mInvitationListener = object : QInvitationHandlerListener {

        override fun onReceivedApply(qInvitation: QInvitation) {}

        override fun onApplyCanceled(qInvitation: QInvitation) {
            MyLinkerInfoDialog.StartLinkStore.isInviting = false
        }

        override fun onApplyTimeOut(qInvitation: QInvitation) {
            MyLinkerInfoDialog.StartLinkStore.isInviting = false
            LoadingDialog.cancelLoadingDialog()
        }

        override fun onAccept(qInvitation: QInvitation) {
            MyLinkerInfoDialog.StartLinkStore.isInviting = false
            LoadingDialog.cancelLoadingDialog()
            context.getString(R.string.invite_be_accept_by_anchor).asToast(context)
            client?.getService(QLinkMicService::class.java)
                ?.audienceMicHandler
                ?.startLink(
                    null, if (MyLinkerInfoDialog.StartLinkStore.isVideoLink) {
                        QCameraParam()
                    } else {
                        null
                    }, QMicrophoneParam(),
                    object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            msg?.asToast(context)
                        }

                        override fun onSuccess(data: Void?) {
                            MyLinkerInfoDialog.StartLinkStore.startTime = System.currentTimeMillis()
                        }
                    }
                )
        }

        override fun onReject(qInvitation: QInvitation) {
            MyLinkerInfoDialog.StartLinkStore.isInviting = false
            context.getString(R.string.invite_be_reject_by_anchor).asToast(context)
            LoadingDialog.cancelLoadingDialog()
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QLinkMicService::class.java).invitationHandler.addInvitationHandlerListener(
            mInvitationListener
        )
        this.setOnClickListener {
            if (roomInfo == null || client == null || user == null) {
                return@setOnClickListener
            }
            //申请中 主播还没有同意
            if (MyLinkerInfoDialog.StartLinkStore.isInviting) {
                context.getString(R.string.invite_waiting_tip).asToast(context)
                return@setOnClickListener
            }
            //我本来就在麦上
            if (client?.getService(QLinkMicService::class.java)?.audienceMicHandler?.isLinked() == true) {
                //显示我的连麦信息
                MyLinkerInfoDialog(client!!.getService(QLinkMicService::class.java), user!!).show(
                    kitContext!!.fragmentManager,
                    ""
                )
                return@setOnClickListener
            }
            //主播PK中 不准申请
            if (client.getService(QPKService::class.java)?.currentPKingSession() != null) {
                context.getString(R.string.link_invite_pking_tip).asToast(context)
                return@setOnClickListener
            }
            //连麦申请弹窗
            LinkApplyDialog().apply {
                mDefaultListener = object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        LoadingDialog.showLoading(kitContext!!.fragmentManager)
                        MyLinkerInfoDialog.StartLinkStore.isVideoLink = any as Boolean

                        //申请上麦
                        client.getService(QLinkMicService::class.java).invitationHandler.apply(
                            15 * 1000,
                            roomInfo!!.liveID,
                            roomInfo!!.anchor.userId,
                            null,
                            object :
                                QLiveCallBack<QInvitation> {
                                override fun onError(code: Int, msg: String?) {
                                    msg?.asToast(context)
                                    LoadingDialog.cancelLoadingDialog()
                                }

                                override fun onSuccess(data: QInvitation) {
                                    MyLinkerInfoDialog.StartLinkStore.isInviting = true
                                    context?.getString(R.string.invite_wait_anchor_tip)
                                        ?.asToast(context)
                                }
                            }
                        )
                    }
                }
            }.show(kitContext!!.fragmentManager, "")
        }
    }

    override fun onDestroyed() {
        client?.getService(QLinkMicService::class.java)?.invitationHandler?.removeInvitationHandlerListener(
            mInvitationListener
        )
        super.onDestroyed()
        MyLinkerInfoDialog.StartLinkStore.isInviting = false
    }
}