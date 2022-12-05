package com.qlive.rtclive

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.qiniu.droid.rtc.QNTextureView

open class QPushTextureView : FrameLayout, RTCRenderView {
    private var renderView: QNTextureView? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
    }

    override fun getView(): View {
        return this
    }

    override fun getQNRender(): QNTextureView {
        if(renderView==null){
            renderView = QNTextureView(context)
            addView(
                renderView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
        return renderView!!
    }
//
//    fun release() {
//        Log.d("mjl"," QPushTextureView relase")
//        renderView?.release()
//    }
}
