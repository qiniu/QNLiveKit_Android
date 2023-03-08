package com.qlive.uikitcore

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.uikitcore.ext.ViewBindingExt
import com.qlive.uikitcore.view.CircleImageView
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

abstract class QKitFrameLayout : FrameLayout, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val layoutId = getLayoutId()
        if (layoutId > 0) {
            LayoutInflater.from(context).inflate(layoutId, this, true)
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        initView()
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()
}

abstract class QKitLinearLayout : LinearLayout, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {

    }
    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        initView()
    }
    abstract fun initView()
}

abstract class QKitViewBindingFrameLayout<T : ViewBinding> : FrameLayout, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null
    lateinit var binding: T

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val sup = javaClass.genericSuperclass
        binding = ViewBindingExt.create(sup as ParameterizedType, this, context, true)
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        initView()
    }

    abstract fun initView()
}

abstract class QKitViewBindingFrameMergeLayout<T : ViewBinding> : FrameLayout, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null
    lateinit var binding: T

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val sup = javaClass.genericSuperclass
        binding = ViewBindingExt.create2(sup as ParameterizedType, this, context)
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        initView()
    }

    abstract fun initView()
}


@SuppressLint("AppCompatCustomView")
open class QKitImageView : ImageView, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}

class QKitCircleImageView : CircleImageView, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}

open class QKitRecyclerView : RecyclerView, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}

@SuppressLint("AppCompatCustomView")
open class QKitTextView : TextView, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}

open class QKitCardView : CardView, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val layoutId = getLayoutId()
        if (layoutId > 0) {
            LayoutInflater.from(context).inflate(layoutId, this, true)
        }
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        initView()
    }

    open fun getLayoutId(): Int {
        return -1
    }

    open fun initView() {

    }
}

open class QKitViewBindingCardView<T : ViewBinding> : CardView, QLiveComponent {
    override var client: QLiveClient? = null
    override var roomInfo: QLiveRoomInfo? = null
    override var user: QLiveUser? = null
    override var kitContext: QLiveUIKitContext? = null
    lateinit var binding: T

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val sup = javaClass.genericSuperclass
        binding = ViewBindingExt.create(sup as ParameterizedType, this, context, true)
    }

    override fun attachLiveClient(client: QLiveClient) {
        super.attachLiveClient(client)
        initView()
    }

    open fun initView() {

    }
}
