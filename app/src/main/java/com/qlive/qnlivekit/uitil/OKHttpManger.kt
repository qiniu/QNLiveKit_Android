package com.qlive.qnlivekit.uitil

import okhttp3.OkHttpClient

object OKHttpManger {
    var okHttp: OkHttpClient = OkHttpClient.Builder()
        .build()
        private set
}