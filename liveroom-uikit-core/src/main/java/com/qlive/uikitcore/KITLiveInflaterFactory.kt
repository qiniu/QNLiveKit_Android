package com.qlive.uikitcore

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
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

    companion object {
        var checkCreateView: (
            name: String,
            context: Context,
            attrs: AttributeSet
        ) -> View? = { _, _, _ ->
            null
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        //优先匹配已知的类 减少反射次数
        var view: View? = checkCreateView(name, context, attrs) ?: appDelegate.createView(
            parent,
            name,
            context,
            attrs
        )

        val len = name.split(".").size
        if (view == null && len > 1) {
            QLiveLogUtil.d("createView by appDelegate == null $name")
            try {
                val viewClass = Class.forName(name)
                if (QLiveComponent::class.java.isAssignableFrom(viewClass)) {
                    QLiveLogUtil.d("createView by constructor.newInstance $name")
                    //使用反射创建没有匹配的类
                    val constructor =
                        viewClass.getConstructor(Context::class.java, AttributeSet::class.java)
                    view = constructor.newInstance(context, attrs) as View
                }
            } catch (e: ClassNotFoundException) {
                QLiveLogUtil.d("createView $name" + e.message ?: "")
            }
        }
        return view
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }
}
