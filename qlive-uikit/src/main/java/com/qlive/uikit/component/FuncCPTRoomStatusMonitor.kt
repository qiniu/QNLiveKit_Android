package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.qlive.core.QClientType
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveStatus
import com.qlive.core.QLiveStatusListener
import com.qlive.core.been.QExtension
import com.qlive.roomservice.QRoomService
import com.qlive.roomservice.QRoomServiceListener
import com.qlive.uikit.R
import com.qlive.uikitcore.QLiveFuncComponent
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.FinalDialogFragment

/**
 * 房间销毁结束页面功能组件
 */
class FuncCPTRoomStatusMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mQLiveStatusListener = QLiveStatusListener { liveStatus, msg -> //如果房主离线 关闭页面
        if (liveStatus == QLiveStatus.OFF || liveStatus == QLiveStatus.FORCE_CLOSE) {
            if (msg.isNotEmpty() && client?.clientType == QClientType.PUSHER && liveStatus == QLiveStatus.FORCE_CLOSE) {
                post {
                    client?.destroy()
                }
                CommonTipDialog.TipBuild()
                    .setTittle(kitContext!!.androidContext.getString(R.string.warn))
                    .setContent(
                        msg
                    ).setListener(object : FinalDialogFragment.BaseDialogListener() {
                        override fun onDismiss(dialog: DialogFragment) {
                            super.onDismiss(dialog)
                            kitContext?.currentActivity?.finish()
                        }
                    })
                    .setPositiveText(kitContext!!.androidContext.getString(R.string.confirm))
                    .isNeedCancelBtn(false)
                    .build("FuncCPTPKApplyMonitor——onReceivedCensorNotify")
                    .show(kitContext!!.fragmentManager, "")

            } else {
                val tip = msg.ifEmpty {
                    context.getString(R.string.live_room_destroyed_tip)
                }
                Toast.makeText(
                    kitContext?.androidContext,
                    tip,
                    Toast.LENGTH_LONG
                ).show()
                kitContext?.currentActivity?.finish()
            }
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.addLiveStatusListener(mQLiveStatusListener)
    }

    override fun onDestroyed() {
        client?.removeLiveStatusListener(mQLiveStatusListener)
        super.onDestroyed()
    }
}

/**
 * 管理后台警告监听
 *
 * @constructor Create empty Func c p t room warn monitor
 */
class FuncCPTRoomWarnMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mRoomServiceListener = object : QRoomServiceListener {
        override fun onRoomExtensionUpdate(extension: QExtension?) {
        }

        override fun onReceivedCensorNotify(message: String) {
            CommonTipDialog.TipBuild()
                .setTittle(kitContext!!.androidContext.getString(R.string.warn))
                .setContent(
                    message
                )
                .setPositiveText(kitContext!!.androidContext.getString(R.string.confirm))
                .isNeedCancelBtn(false)
                .build("FuncCPTPKApplyMonitor——onReceivedCensorNotify")
                .show(kitContext!!.fragmentManager, "")
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QRoomService::class.java).addRoomServiceListener(mRoomServiceListener)
    }

    override fun onDestroyed() {
        client?.getService(QRoomService::class.java)
            ?.removeRoomServiceListener(mRoomServiceListener)
        super.onDestroyed()
    }
}

/**
 * 房主掉线结束页面功能组件
 */
class FuncCPTAnchorStatusMonitor : QLiveFuncComponent {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mQLiveStatusListener = QLiveStatusListener { liveStatus, _ -> //如果房主离线 关闭页面
        if (liveStatus == QLiveStatus.ANCHOR_OFFLINE) {
            Toast.makeText(
                kitContext?.androidContext,
                R.string.live_anchor_offline_tip,
                Toast.LENGTH_SHORT
            ).show()
            kitContext?.currentActivity?.finish()
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.addLiveStatusListener(mQLiveStatusListener)
    }

    override fun onDestroyed() {
        client?.removeLiveStatusListener(mQLiveStatusListener)
        super.onDestroyed()
    }
}
