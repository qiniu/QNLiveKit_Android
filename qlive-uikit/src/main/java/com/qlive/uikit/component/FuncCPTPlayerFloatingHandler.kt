package com.qlive.uikit.component

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.qlive.core.QLiveCallBack
import com.qlive.liblog.QLiveLogUtil
import com.qlive.linkmicservice.QLinkMicService
import com.qlive.playerclient.QPlayerClient
import com.qlive.uikitcore.floating.*
import com.qlive.uikitcore.floating.permission.PermissionUtils
import com.qlive.uikitcore.floating.uitls.DefaultAnimator
import com.qlive.uikitcore.floating.uitls.DisplayUtils
import com.qlive.uikitcore.floating.uitls.LifecycleUtils
import com.qlive.qplayer.QPlayerTextureRenderView
import com.qlive.roomservice.QRoomService
import com.qlive.sdk.QLive
import com.qlive.uikit.R
import com.qlive.uikit.RoomPage
import com.qlive.uikitcore.QLiveFuncComponent
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.FinalDialogFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *小窗模式
 */
enum class FloatingModel {
    /**
     * 去桌面
     */
    GO_DESKTOP,

    /**
     * 返回上一个页面
     * activity销毁小窗
     */
    BACK_LAST_PAGE,

    /**
     * 去下一页面
     * activity不销毁-小窗
     */
    GO_NEXT_PAGE
}

class FuncCPTPlayerFloatingHandler(context: Context) : QLiveFuncComponent(context, null) {

    internal var isGoingToRequestFloatPermission = false
        private set

    companion object {
        internal const val playerTAG = "FloatingPlayer"

        @SuppressLint("StaticFieldLeak")
        var currentFloatingPlayerView: FloatingPlayerView? = null

        /**
         * 权限申请提示回调
         * Permission request tip call
         * @param afterTipCall 自定义权限申请提示
         * afterTipCall.invoke(true) == 已经提示完成可以去申请
         * afterTipCall.invoke(false) == 不去申请，取消操作
         */
        var permissionRequestTipCall: (kitConText: QLiveUIKitContext, afterTipCall: (Boolean) -> Unit) -> Unit =
            { kitConText, afterTipCall ->

                CommonTipDialog.TipBuild()
                    .setTittle(kitConText.androidContext.getString(R.string.tip))
                    .setContent(kitConText.androidContext.getString(R.string.live_float_permission_tip))
                    .setPositiveText(kitConText.androidContext.getString(R.string.live_goto_float_permission_tip))
                    .build("permissionRequestTipCall")
                    .apply {
                        applyCancelable(false)
                        mDefaultListener = object : FinalDialogFragment.BaseDialogListener() {
                            override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                                super.onDialogPositiveClick(dialog, any)
                                afterTipCall.invoke(true)
                            }

                            override fun onDialogNegativeClick(dialog: DialogFragment, any: Any) {
                                super.onDialogNegativeClick(dialog, any)
                                afterTipCall.invoke(false)
                            }
                        }
                    }
                    .show(kitConText.fragmentManager, "")
            }

        /**
         * 悬浮配置
         */
        var createDefaultFloatConfig: (context: Context) -> QFloatConfig = { context ->
            QFloatConfigBuilder()
                // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
                .setShowPattern(ShowPattern.ALL_TIME)
                // 设置吸附方式，共15种模式，详情参考SidePattern
                .setSidePattern(SidePattern.RESULT_HORIZONTAL)
                // 设置浮窗是否可拖拽
                .setDragEnable(true)
                // 设置浮窗的对齐方式和坐标偏移量
                .setGravity(Gravity.END or Gravity.BOTTOM, -20, -200)
                // 设置当布局大小变化后，整体view的位置对齐方式
                .setLayoutChangedGravity(Gravity.CENTER)
                // 设置拖拽边界值
                .setBorder(
                    20, DisplayUtils.getScreenWidth(context) - 20,
                    -DisplayUtils.getStatusBarHeight(context),
                    DisplayUtils.getScreenHeight(context)
                )
                // 设置宽高是否充满父布局，直接在xml设置match_parent属性无效
                .setMatchParent(widthMatch = false, heightMatch = false)
                // 设置浮窗的出入动画，可自定义，实现相应接口即可（策略模式），无需动画直接设置为null
                .setAnimator(DefaultAnimator())
                .build()
        }
    }

    /**
     * 开始悬浮播放
     * @param floatingModel 悬浮窗模式
     * @param view 自定义小窗UI 默认DefaultFloatingPlayerView
     * @param config  小窗的位置等配置参数
     * @param createCall 成功失败回调
     */
    fun create(
        floatingModel: FloatingModel,
        view: FloatingPlayerView = DefaultFloatingPlayerView(),
        config: QFloatConfig = createDefaultFloatConfig(kitContext!!.androidContext),
        createCall: (Boolean, String) -> Unit
    ) {
        if (kitContext == null) {
            createCall.invoke(false, "kitContext==null")
            return
        }
        if (client?.getService(QLinkMicService::class.java)?.audienceMicHandler?.isLinked == true) {
            createCall.invoke(false, "连麦中不支持小窗口")
            return
        }

        val createAction: (needRequestPermission: Boolean) -> Unit = { needRequestPermission ->
            isGoingToRequestFloatPermission = needRequestPermission
            view.create(
                kitContext!!.currentActivity as FragmentActivity,
                config,
            ) { ret, msg ->
                isGoingToRequestFloatPermission = false
                createCall.invoke(ret, msg)
                if (ret) {
                    val textureRenderView =
                        view.getPlayerTextureRenderView()
                    (client as QPlayerClient).play(textureRenderView)
                    view.clientRef = client as QPlayerClient
                    view.floatingModel = floatingModel
                    view.taskId = kitContext?.currentActivity?.taskId ?: 0
                    when (floatingModel) {
                        FloatingModel.GO_DESKTOP -> {
                            view.activityRef = WeakReference(kitContext!!.currentActivity)
                            kitContext?.currentActivity?.moveTaskToBack(true)
                        }
                        FloatingModel.GO_NEXT_PAGE -> {
                            view.activityRef = WeakReference(kitContext!!.currentActivity)
                        }
                        FloatingModel.BACK_LAST_PAGE -> {
                            kitContext?.currentActivity?.finish()
                        }
                    }
                }
            }
        }

        if (!PermissionUtils.checkPermission(kitContext!!.currentActivity)) {
            permissionRequestTipCall.invoke(kitContext!!) {
                if (it) {
                    createAction.invoke(true)
                } else {
                    createCall.invoke(false, "取消了申请权限")
                }
            }
        } else {
            createAction.invoke(false)
        }
    }

    /**
    播放器悬浮窗
     */
    abstract class FloatingPlayerView : QFloatingWindow() {

        abstract fun getPlayerTextureRenderView(): QPlayerTextureRenderView
        internal var activityRef: WeakReference<Activity>? = null
        internal var clientRef: QPlayerClient? = null
        internal var floatingModel = FloatingModel.GO_DESKTOP
        internal var taskId = 0

        override fun onDismiss() {
            super.onDismiss()
            currentFloatingPlayerView = null
        }

        override fun onCreate(view: View) {
            super.onCreate(view)
            currentFloatingPlayerView = this@FloatingPlayerView
        }

        private suspend fun suspendLeftForce() = suspendCoroutine<Unit> { cont ->
            clientRef!!.leaveRoom(object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    cont.resume(Unit)
                }

                override fun onSuccess(data: Void?) {
                    cont.resume(Unit)
                }
            })
        }

        fun close() {
            super.dismiss(true)
            val act = activityRef?.get()
            GlobalScope.launch {
                suspendLeftForce()
                if (act == null) {
                    clientRef?.destroy()
                } else {
                    act.finish()
                }
                activityRef?.clear()
                clientRef = null
                activityRef = null
            }
        }

        override fun dismiss(force: Boolean) {
            super.dismiss(force)
            activityRef?.clear()
            clientRef = null
            activityRef = null
        }

        //
        @SuppressLint("MissingPermission")
        fun resumeToBig() {
            QLiveLogUtil.d("FuncCPTPlayerFloatingHandler", "resumeToBig")
            val roomID = clientRef?.getService(QRoomService::class.java)?.roomInfo?.liveID
            if (TextUtils.isEmpty(roomID)) {
                dismiss()
                return
            }
            if (floatingModel == FloatingModel.GO_DESKTOP) {
                attachView?.isClickable = false
                LifecycleUtils.moveTaskToFront(taskId) {
                    attachView?.isClickable = true
                }
                return
            } else {
                if (LifecycleUtils.isForeGround()) {
                    QLive.getLiveUIKit().getPage(RoomPage::class.java)
                        .startPlayerRoomActivity(
                            LifecycleUtils.getTopActivity()!!,
                            roomID!!, null
                        )
                } else {
                    attachView?.isClickable = false
                    LifecycleUtils.moveTaskToFront(taskId) {
                        attachView?.isClickable = true
                        QLive.getLiveUIKit().getPage(RoomPage::class.java)
                            .startPlayerRoomActivity(
                                LifecycleUtils.getTopActivity()!!,
                                roomID!!, null
                            )
                    }
                }
            }
        }
    }
}