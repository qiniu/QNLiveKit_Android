package com.qlive.uikit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat
import com.qlive.uikitcore.QLiveOutRoomComponentManager
import com.qlive.uikitcore.QUIKitContext
import com.qlive.uikitcore.activity.BaseFrameActivity

class LiveRecordActivity : BaseFrameActivity() {

    companion object {
        var replaceLayoutID = -1
        fun start(context: Context) {
            context.startActivity(Intent(context, LiveRecordActivity::class.java))
        }
    }

    private val mQUIKitContext by lazy {
        QUIKitContext(
            this@LiveRecordActivity,
            supportFragmentManager,
            this@LiveRecordActivity,
            this@LiveRecordActivity
        )
    }
    private val uiFactory by lazy { QLiveOutRoomComponentManager(mQUIKitContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        uiFactory.scanComponent(decorView)
    }

    override fun onDestroy() {
        uiFactory.onDestroyed()
        super.onDestroy()
    }

    override fun init() {
    }

    override fun getLayoutId(): Int {
        if (replaceLayoutID > 0) {
            return replaceLayoutID
        }
        return R.layout.kit_activity_live_record
    }
}