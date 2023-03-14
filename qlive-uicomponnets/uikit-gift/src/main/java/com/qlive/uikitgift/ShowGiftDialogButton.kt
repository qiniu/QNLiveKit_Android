package com.qlive.uikitgift

import android.content.Context
import android.util.AttributeSet
import com.qlive.uikitcore.QKitLinearLayout

class ShowGiftDialogButton : QKitLinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mGiftDialog by lazy { GiftDialog(client!!) }
    override fun initView() {
        setOnClickListener {
            mGiftDialog.show(kitContext!!.fragmentManager, "")
        }
    }
}