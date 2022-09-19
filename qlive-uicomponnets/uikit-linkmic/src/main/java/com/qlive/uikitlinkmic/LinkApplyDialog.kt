package com.qlive.uikitlinkmic

import android.Manifest
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.ext.permission.PermissionAnywhere
import kotlinx.android.synthetic.main.kit_dialog_apply.*

/**
 * 观众连麦申请弹窗
 */
class LinkApplyDialog : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_apply
    }

    override fun init() {
        llAudio.setOnClickListener {
            request(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                dismiss()
                mDefaultListener?.onDialogPositiveClick(this, false)
            }
        }
        llVideo.setOnClickListener {
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
                Toast.makeText(requireContext(), getString(R.string.live_permission_check_tip), Toast.LENGTH_SHORT).show()
            }
        }
    }

}