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
    private val appDelegate: AppCompatDelegate,
    private val roomClient: QLiveClient,
    private val kitContext: QLiveUIKitContext
) : LayoutInflater.Factory2, QLiveUILifeCycleListener {

    val mComponents = HashSet<QLiveComponent>()

    init {
        kitContext.mAllComponentCall = {
            mComponents.toList()
        }
    }

    fun addFuncComponent(funcComponent: QLiveFuncComponent) {
        mComponents.add(funcComponent)
        funcComponent.attachKitContext(kitContext)
        funcComponent.attachLiveClient(roomClient)
    }

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
        if (view is QLiveComponent) {
            //   QLiveLogUtil.d("KITInflaterFactory", "onCreateView " + name + " attachKitContext ")
            (view as QLiveComponent).attachKitContext(kitContext)
            (view as QLiveComponent).attachLiveClient(roomClient)
            mComponents.add(view)
        }
        return view
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    override fun onEntering(liveId: String, user: QLiveUser) {
        mComponents.forEach {
            it.onEntering(liveId, user)
        }
    }

    override fun onGetLiveRoomInfo(roomInfo: QLiveRoomInfo) {
        mComponents.forEach {
            it.onGetLiveRoomInfo(roomInfo)
        }
    }

    override fun onJoined(roomInfo: QLiveRoomInfo, isResumeUIFromFloating: Boolean) {
        mComponents.forEach {
            it.onJoined(roomInfo, isResumeUIFromFloating)
        }
    }

    override fun onLeft() {
        mComponents.forEach {
            it.onLeft()
        }
    }

    override fun onDestroyed() {
        kitContext.eventManager.clear()
        mComponents.forEach {
            it.onDestroyed()
        }
        mComponents.clear()
    }
}

/**
 * 直播间外UI组件装载器
 */
class KITInflaterFactory(
    private val appDelegate: AppCompatDelegate,

    private val kitContext: QUIKitContext
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

    private val mComponents = HashSet<QComponent>()

    init {
        kitContext.mAllComponentCall = {
            mComponents.toList()
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        var view = checkCreateView(name, context, attrs) ?: appDelegate.createView(
            parent,
            name,
            context,
            attrs
        )
        val len = name.split(".").size
        if (view == null && len > 1) {
            try {
                val viewClass = Class.forName(name)
                if (QComponent::class.java.isAssignableFrom(viewClass)) {
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
        if (view is QComponent) {
            mComponents.add(view)
            (view as QComponent).attachKitContext(kitContext)
        }
        return view
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    fun onDestroyed() {
        kitContext.eventManager.clear()
        mComponents.clear()
    }
}


/**
 * 直播间外从直播间跳转去依赖直播信息页面UI组件装载器
 */
class KITRoomDependsInflaterFactory(
    private val appDelegate: AppCompatDelegate,
    private val roomClient: QLiveClientClone,
    private val kitContext: QLiveUIKitContext,

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

    val mComponents = HashSet<QRoomComponent>()

    init {
        kitContext.mAllComponentCall = {
            mComponents.toList()
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {

        var view = checkCreateView(name, context, attrs) ?: appDelegate.createView(
            parent,
            name,
            context,
            attrs
        )

        val len = name.split(".").size
        if (view == null && len > 1) {
            try {
                val viewClass = Class.forName(name)
                if (QRoomComponent::class.java.isAssignableFrom(viewClass)) {
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

        if (view is QRoomComponent) {
            mComponents.add(view)
            (view as QRoomComponent).attachKitContext(kitContext)
            (view as QRoomComponent).attachLiveClient(roomClient)
        }
        return view
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    fun onDestroyed() {
        kitContext.eventManager.clear()
        mComponents.clear()
    }
}
