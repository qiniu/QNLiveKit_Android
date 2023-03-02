package com.qlive.qnlivekit

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QExtension
import com.qlive.pkservice.QPKService
import com.qlive.pkservice.QPKServiceListener
import com.qlive.pkservice.QPKSession
import com.qlive.uikitcore.QKitTextView

class TestPKEXTButton : QKitTextView {

    var clientNum = 1

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setOnClickListener {
            val pkInfo = client?.getService(QPKService::class.java)?.currentPKingSession()
                ?: return@setOnClickListener
            client?.getService(QPKService::class.java)?.updateExtension(QExtension().apply {
                key = "test"
                value = "${clientNum++}"
            }, object : QLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {

                }

                override fun onSuccess(data: Void?) {

                }
            })
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        client.getService(QPKService::class.java)
            .addServiceListener(object : QPKServiceListener {
                //pk开始
                override fun onStart(pkSession: QPKSession) {
                    //如果后面进来的观众进入一个已经pk进行中的房间，取出最新的自定义字段的值恢复UI
//                    pkSession.extension["pk_score"]?.let {}
//                    pkSession.extension["pk_win_or_lose"]?.let {}
                }

                override fun onStop(pkSession: QPKSession, code: Int, msg: String) {}
                override fun onStartTimeOut(pkSession: QPKSession) {}

                //pk自定义扩展字段跟新
                override fun onPKExtensionChange(extension: QExtension) {
                    when (extension.key) {
                        //自定义pk分数事件
                        "pk_score" -> {}
                        //pk输赢事件
                        "pk_win_or_lose" -> {}
                    }
                }
            })
    }

}