package com.qlive.uikit.component

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qlive.core.*
import com.qlive.uikit.R
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.core.been.QCreateRoomParam
import com.qlive.sdk.QLive
import com.qlive.uikit.RoomPushActivity.Companion.KEY_ROOM_ID
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

/**
 * 开播预览槽位
 */
open class LivePreView : QKitFrameLayout {

    companion object {
        /**
         * 设置房间参数回调
         */
        var makeCreateRoomParamCall: (kitContext: QLiveUIKitContext, client: QLiveClient, titleStr: String, noticeStr: String) -> QCreateRoomParam =
            { _, _, titleStr: String, noticeStr: String ->
                QCreateRoomParam().apply {
                    title = titleStr
                    notice = noticeStr
                    coverURL = QLive.getLoginUser()?.avatar ?: ""
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

    override fun getLayoutId(): Int {
        return R.layout.kit_live_preview
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        super.onJoined(roomInfo, isResumeUIFromFloating)
        //开播预览加入成功不可见
        visibility = View.GONE
    }

    override fun initView() {
        findViewById<View>(R.id.tvStart).setDoubleCheckClickListener {
            val titleStr = findViewById<EditText>(R.id.etTitle).text.toString()
            if (titleStr.isEmpty()) {
                context?.getString(R.string.preview_hit_room_title)?.asToast(context)
                return@setDoubleCheckClickListener
            }
            val noticeStr = findViewById<EditText>(R.id.etNotice).text.toString() ?: ""
            //开始创建并且加入房间
            kitContext?.createAndJoinRoomActionCall?.invoke(
                makeCreateRoomParamCall(kitContext!!, client!!, titleStr, noticeStr),
                object : QLiveCallBack<Void> {
                    override fun onError(code: Int, msg: String?) {}
                    override fun onSuccess(data: Void?) {}
                })
        }
    }
}