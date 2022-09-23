package com.qlive.sdk.internal

import android.content.Context

internal class AppCache {
    companion object {
        lateinit var appContext: Context
            private set

        fun setContext(context: Context) {
            appContext = context
        }
    }
}