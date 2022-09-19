package com.qlive.uikitcore.floating.permission
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

internal class PermissionFragment : Fragment() {

    companion object {
        private var onPermissionResult: OnPermissionResult? = null

        fun requestPermission(activity: FragmentActivity, onPermissionResult: OnPermissionResult) {
            this.onPermissionResult = onPermissionResult
            activity.supportFragmentManager
                .beginTransaction()
                .add(PermissionFragment(), activity.localClassName)
                .commitAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 权限申请
        PermissionUtils.requestPermission(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PermissionUtils.requestCode) {
            // 需要延迟执行，不然即使授权，仍有部分机型获取不到权限
            Handler(Looper.getMainLooper()).postDelayed({
                val activity = activity ?: return@postDelayed
                val check = PermissionUtils.checkPermission(activity)
                // 回调权限结果
                onPermissionResult?.permissionResult(check)
                onPermissionResult = null
                // 将Fragment移除
                activity.supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
            }, 500)
        }
    }

}
