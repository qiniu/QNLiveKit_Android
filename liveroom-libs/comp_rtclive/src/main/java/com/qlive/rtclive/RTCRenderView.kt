package com.qlive.rtclive

import com.qiniu.droid.rtc.QNRenderView
import com.qlive.avparam.QPushRenderView

interface RTCRenderView: QPushRenderView {
    fun getQNRender(): QNRenderView
}