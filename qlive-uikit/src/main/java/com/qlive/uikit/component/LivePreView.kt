package com.qlive.uikit.component

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qlive.core.*
import com.qlive.core.been.QCreateRoomParam
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.sdk.QLive
import com.qlive.uikit.R
import com.qlive.uikit.RoomPushActivity.Companion.KEY_ROOM_ID
import com.qlive.uikit.databinding.KitLivePreviewBinding
import com.qlive.uikitcore.QKitViewBindingFrameMergeLayout
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

/**
 * 开播预览槽位
 */
open class LivePreView : QKitViewBindingFrameMergeLayout<KitLivePreviewBinding> {

    companion object {
        /**
         * 设置房间参数回调
         */
        var makeCreateRoomParamCall: (kitContext: QLiveUIKitContext, client: QLiveClient, titleStr: String, noticeStr: String, startTime: Long) -> QCreateRoomParam =
            { _, _, titleStr: String, noticeStr: String, startTime: Long ->
                QCreateRoomParam().apply {
                    title = titleStr
                    notice = noticeStr
                    coverURL = QLive.getLoginUser()?.avatar ?: ""
                    startAt = startTime
                }
            }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_CREATE) {
            //进入已经创建好的直播间不可见
            val roomId = kitContext!!.currentActivity.intent.getStringExtra(KEY_ROOM_ID) ?: ""
            if (!TextUtils.isEmpty(roomId)) {
                visibility = View.GONE
            }
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isJoinedBefore: Boolean) {
        super.onJoined(roomInfo, isJoinedBefore)
        //开播预览加入成功不可见
        visibility = View.GONE
    }

    private var reserveTime = 0L
    override fun initView() {
        binding.tvStart.setDoubleCheckClickListener {
            val titleStr = binding.etTitle.text.toString()
            if (titleStr.isEmpty()) {
                context?.getString(R.string.preview_hit_room_title)?.asToast(context)
                return@setDoubleCheckClickListener
            }
            val noticeStr = binding.etNotice.text.toString() ?: ""

            if (binding.rgLiveMode.checkedRadioButtonId == R.id.rbLiveNow) {
                //开始创建并且加入房间
                kitContext?.startPusherRoomActionCall?.invoke(
                    makeCreateRoomParamCall(kitContext!!, client!!, titleStr, noticeStr, 0),
                    object : QLiveCallBack<QLiveRoomInfo> {
                        override fun onError(code: Int, msg: String?) {}
                        override fun onSuccess(data: QLiveRoomInfo?) {
                            Log.d("mjl"," startPusherRoomActionCall"+data.toString())
                        }
                    })
            } else {
                if (reserveTime == 0L) {
                    context?.getString(R.string.preview_hit_select_date)?.asToast(context)
                    return@setDoubleCheckClickListener
                }
                val param = makeCreateRoomParamCall(
                    kitContext!!,
                    client!!,
                    titleStr,
                    noticeStr,
                    reserveTime/1000
                )
                LoadingDialog.showLoading(kitContext!!.fragmentManager)
                QLive.getRooms().createRoom(param, object : QLiveCallBack<QLiveRoomInfo> {
                    override fun onError(code: Int, msg: String?) {
                        msg?.asToast(context)
                        LoadingDialog.cancelLoadingDialog()
                    }

                    override fun onSuccess(data: QLiveRoomInfo) {
                        "创建成功".asToast(context)
                        LoadingDialog.cancelLoadingDialog()
                        kitContext?.currentActivity?.finish()
                    }
                })
            }
        }

        binding.rgLiveMode.setOnCheckedChangeListener { radioGroup, id ->
            if (id == R.id.rbLiveNow) {
                binding.tvCalendar.visibility = GONE
                binding.tvCalendarHit.visibility = GONE
            } else {
                binding.tvCalendar.visibility = visibility
                binding.tvCalendarHit.visibility = visibility
            }
        }

        binding.tvCalendar.setOnClickListener {
            TimePickPop(context).apply {
                onTimeSelectedCall = { timeLong, timeFormat ->
                    binding.tvCalendar.text = timeFormat
                    reserveTime = timeLong
                }
            }.show(binding.popAnchorView)
        }
        binding.rgLiveMode.check(R.id.rbLiveNow)
    }
}