package com.qlive.uikitcore.ext

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.Spanned
import android.view.View
import android.widget.Toast


fun String.toHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}


private var mLastClickTime = 0L
fun View.setDoubleCheckClickListener(call: (view: View) -> Unit) {
    this.setOnClickListener {
        val now = System.currentTimeMillis()
        if (now - mLastClickTime > 500) {
            call.invoke(it)
        }
        mLastClickTime = now
    }
}

fun String.asToast(context: Context?) {
    if (this.isEmpty()) {
        return
    }
    if (context == null) {
        return
    }
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}
