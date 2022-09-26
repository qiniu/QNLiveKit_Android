package com.qlive.uikitpublicchat

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.Toast
import com.qlive.core.QLiveCallBack
import com.qlive.pubchatservice.QPublicChat
import com.qlive.pubchatservice.QPublicChatService
import com.qlive.uikitcore.QKitFrameLayout
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitinput.RoomInputDialog

//公屏幕输入框
class InputView : QKitFrameLayout {

    companion object {
        /**
         * 发送过滤回调
         */
        var sendFilter: (kitContext: QLiveUIKitContext, msg: String, call: (canSend: Boolean) -> Unit) -> Unit =
            { _, _, call ->
                call(true)
            }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setOnClickListener {
            Log.d("mjl","kitContext"+kitContext.toString())
            RoomInputDialog().apply {
                sendPubCall = { msg ->
                    if (kitContext != null) {
                        sendFilter.invoke(kitContext!!, msg) {
                            if (it) {
                                client?.getService(QPublicChatService::class.java)
                                    ?.sendPublicChat(msg, object :
                                        QLiveCallBack<QPublicChat> {
                                        override fun onError(code: Int, msg: String) {
                                            Toast.makeText(
                                                context,
                                                "发送失败" + msg,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        override fun onSuccess(data: QPublicChat?) {
                                        }
                                    })
                            }
                        }
                    }
                }
            }.show(kitContext!!.fragmentManager, "")
        }
    }

    override fun getLayoutId(): Int {
        return -1
    }

    override fun initView() {
    }
}