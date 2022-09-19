package com.qlive.qnim

import android.content.Context
import com.qlive.rtm.RtmManager

object QNIMManager {

    val mRtmAdapter by lazy { com.qlive.qnim.QNIMAdapter() }

    fun init(appId: String, context: Context) {
        mRtmAdapter.init(QNIMConfig.imSDKConfigGetter.invoke(appId, context), context)
        RtmManager.setRtmAdapter(mRtmAdapter)
    }
}