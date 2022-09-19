package com.qlive.coreimpl

import android.content.Context

object AppCache {
    lateinit var appContext: Context
        private set

    internal fun setContext(context: Context) {
        appContext = context
    }
}