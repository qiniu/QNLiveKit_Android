package com.qlive.uikitlinkmic

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.linkmicservice.QMicLinker
import com.qlive.core.*
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.Scheduler
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import kotlinx.android.synthetic.main.kit_dialog_my_linker_info.*
import java.text.DecimalFormat

/**
 * 我的连麦信息弹窗
 */
class MyLinkerInfoDialog(val service: QLinkMicService, val me: QLiveUser) :
    FinalDialogFragment() {

    public object StartLinkStore {
        var isInviting = false
        var isVideoLink = false
        var startTime = 0L
    }

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private var timeDiff = 0

    @SuppressLint("SetTextI18n")
    private val mScheduler = Scheduler(1000) {
        tvTime?.text = "连麦中，通话${formatTime(timeDiff)}"
        timeDiff++
    }

    private fun formatTime(time: Int): String {
        val decimalFormat = DecimalFormat("00")
        val hh: String = decimalFormat.format(time / 3600)
        val mm: String = decimalFormat.format(time % 3600 / 60)
        val ss: String = decimalFormat.format(time % 60)
        return if (hh == "00") {
            "$mm:$ss"
        } else {
            "$hh:$mm:$ss"
        }
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_my_linker_info
    }

    override fun init() {
        val isVideo = StartLinkStore.isVideoLink
        timeDiff = ((System.currentTimeMillis() - StartLinkStore.startTime) / 1000).toInt()
        mScheduler.start()
        if (isVideo) {
            tvTile.text = getString(R.string.link_dialog_mylink_info_video)
            ivCameraStatus.visibility = View.VISIBLE
        } else {
            tvTile.text = getString(R.string.link_dialog_mylink_info_audio)
            ivCameraStatus.visibility = View.INVISIBLE
        }
        refreshInfo()
        ivCameraStatus.setDoubleCheckClickListener {
            service.audienceMicHandler.muteCamera(ivCameraStatus.isSelected,
                object : QLiveCallBack<Boolean> {
                    override fun onError(code: Int, msg: String?) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(data: Boolean) {
                        refreshInfo()
                    }
                })
        }
        ivMicStatus.setDoubleCheckClickListener {
            service.audienceMicHandler.muteMicrophone(ivMicStatus.isSelected,
                object : QLiveCallBack<Boolean> {
                    override fun onError(code: Int, msg: String?) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(data: Boolean) {
                        refreshInfo()
                    }
                })
        }

        Glide.with(requireContext())
            .load(me.avatar)
            .into(ivAvatar)
        tvHangup.setDoubleCheckClickListener {
            LoadingDialog.showLoading(childFragmentManager)
            service.audienceMicHandler.stopLink(object :
                QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    LoadingDialog.cancelLoadingDialog()
                }

                override fun onSuccess(data: Void?) {
                    LoadingDialog.cancelLoadingDialog()
                    dismiss()
                }
            })
        }
    }

    private fun refreshInfo() {
        var myMic: QMicLinker? = null
        service.allLinker.forEach {
            if (it.user.userId == me.userId) {
                myMic = it
                return@forEach
            }
        }
        myMic ?: return
        ivCameraStatus.isSelected = myMic!!.isOpenCamera
        ivMicStatus.isSelected = myMic!!.isOpenMicrophone
    }

    override fun onDismiss(dialog: DialogInterface) {
        mScheduler.cancel()
        super.onDismiss(dialog)
    }
}