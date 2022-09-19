package com.qlive.uikit.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.qlive.avparam.PreviewMode
import com.qlive.liblog.QLiveLogUtil
import com.qlive.uikitcore.floating.QFloatingWindow
import com.qlive.qplayer.QPlayerTextureRenderView
import com.qlive.uikit.R
import com.qlive.uikitcore.ext.setDoubleCheckClickListener

class DefaultFloatingPlayerView() : FuncCPTPlayerFloatingHandler.FloatingPlayerView() {

    override fun getPlayerTextureRenderView(): QPlayerTextureRenderView {
        return attachView!!.findViewById<QPlayerTextureRenderView>(R.id.floatPlayerRenderView)
    }

    override fun getLayoutID(): Int {
        return R.layout.kit_view_floating_play
    }

    override fun onCreate(view: View) {
        super.onCreate(view)
        val closeView = attachView?.findViewById<ImageView>(R.id.ivFloatClose)
        closeView?.setDoubleCheckClickListener {
            close()
        }
        attachView?.setDoubleCheckClickListener {
            resumeToBig()
        }
    }
}

class DefaultFloatingPlayerRenderView : QPlayerTextureRenderView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setVideoSize(width: Int, height: Int) {
        QLiveLogUtil.d(
            "mIMediaPlayer", "setVideoSize  ${width} ${height}  "
        )
        if (width / height.toFloat() < 12 / 16f) {
            setDisplayAspectRatio(PreviewMode.ASPECT_RATIO_PAVED_PARENT)
        } else {
            setDisplayAspectRatio(PreviewMode.ASPECT_RATIO_FIT_PARENT)
        }
        super.setVideoSize(width, height)
    }
}
