package com.qlive.uikitcore

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.qlive.core.QClientLifeCycleListener
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.liblog.QLiveLogUtil

/**
 * 直播间内 UI装载器
 */
class KITLiveInflaterFactory(
    private val appDelegate: AppCompatDelegate
) : LayoutInflater.Factory2 {

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        val componentTag =
            attrs.getAttributeValue("http://schemas.android.com/apk/res-auto", "componentTag")
        if (componentTag?.isNotEmpty() == true && !UIJsonConfigurator.checkEnable(componentTag)) {
            QLiveLogUtil.d("createView ==componentTag != null $componentTag")
            return LazyDeleteView(context)
        }
        return appDelegate.createView(parent, name, context, attrs)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }
}

class LazyDeleteView : ViewGroup {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        visibility = View.GONE
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        (parent as ViewGroup?)?.removeView(this)
    }
}
