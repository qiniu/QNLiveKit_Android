package com.qlive.uikitlinkmic

import android.Manifest
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.permission.PermissionAnywhere
import com.qlive.uikitlinkmic.databinding.KitDialogLinkApplyBinding

/**
 * 观众连麦申请弹窗
 */
class LinkApplyDialog : ViewBindingDialogFragment<KitDialogLinkApplyBinding>() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    override fun init() {
        binding.llAudio.setOnClickListener {
            request(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                dismiss()
                mDefaultListener?.onDialogPositiveClick(this, false)
            }
        }
        binding.llVideo.setOnClickListener {
            request(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                dismiss()
                mDefaultListener?.onDialogPositiveClick(this, true)
            }
        }
    }

    private fun request(permissions: Array<String>, call: (Boolean) -> Unit) {
        PermissionAnywhere.requestPermission(
            requireActivity() as AppCompatActivity,
            permissions
        ) { grantedPermissions, _, _ ->
            if (grantedPermissions.size == permissions.size) {
                call.invoke(true)
            } else {
                call.invoke(false)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.live_permission_check_tip),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}