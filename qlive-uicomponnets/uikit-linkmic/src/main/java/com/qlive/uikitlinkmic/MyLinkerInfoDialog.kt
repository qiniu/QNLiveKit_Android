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
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitlinkmic.databinding.KitDialogMyLinkerInfoBinding
import java.text.DecimalFormat

/**
 * 我的连麦信息弹窗
 */
class MyLinkerInfoDialog(val service: QLinkMicService, val me: QLiveUser) :
    ViewBindingDialogFragment<KitDialogMyLinkerInfoBinding>() {

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
        binding.tvTime.text = "连麦中，通话${formatTime(timeDiff)}"
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

    override fun init() {
        val isVideo = StartLinkStore.isVideoLink
        timeDiff = ((System.currentTimeMillis() - StartLinkStore.startTime) / 1000).toInt()
        mScheduler.start()
        if (isVideo) {
            binding.tvTile.text = getString(R.string.link_dialog_mylink_info_video)
            binding.ivCameraStatus.visibility = View.VISIBLE
        } else {
            binding.tvTile.text = getString(R.string.link_dialog_mylink_info_audio)
            binding.ivCameraStatus.visibility = View.INVISIBLE
        }
        refreshInfo()
        binding.ivCameraStatus.setDoubleCheckClickListener {
            service.audienceMicHandler.muteCamera(binding.ivCameraStatus.isSelected,
                object : QLiveCallBack<Boolean> {
                    override fun onError(code: Int, msg: String?) {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(data: Boolean) {
                        refreshInfo()
                    }
                })
        }
        binding.ivMicStatus.setDoubleCheckClickListener {
            service.audienceMicHandler.muteMicrophone(binding.ivMicStatus.isSelected,
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
            .into(binding.ivAvatar)
        binding.tvHangup.setDoubleCheckClickListener {
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
        binding.ivCameraStatus.isSelected = myMic!!.isOpenCamera
        binding.ivMicStatus.isSelected = myMic!!.isOpenMicrophone
    }

    override fun onDismiss(dialog: DialogInterface) {
        mScheduler.cancel()
        super.onDismiss(dialog)
    }
}