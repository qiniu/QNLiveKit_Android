package com.qlive.coreimpl.util
import android.content.res.Resources
import android.widget.Toast
import com.qlive.coreimpl.AppCache


fun String.asToast() {
    if (this.isEmpty()) {
        return
    }
    Toast.makeText(AppCache.appContext, this, Toast.LENGTH_SHORT).show()
}

fun Resources.toast(res: Int) {
    getString(res).asToast()
}

