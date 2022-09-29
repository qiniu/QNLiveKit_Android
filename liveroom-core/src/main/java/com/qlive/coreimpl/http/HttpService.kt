package com.qlive.coreimpl.http

import com.qlive.core.QTokenGetter
import java.lang.reflect.Type
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

abstract class HttpService {
    companion object {
        val httpClient: HttpService = URLConnectionHttpService()
    }

    var tokenGetter: QTokenGetter? = null
    var baseUrl = "https://live-api.qiniu.com"//"http://10.200.20.28:8099"
    var token = ""
    protected val bzCodeNoError = 0
    protected val mExecutorService = ThreadPoolExecutor(
        8, 100,
        60L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>()
    )

    abstract suspend fun <T> put(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ): T

    abstract suspend fun <T> post(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ): T

    abstract suspend fun <T> delete(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ): T

    abstract suspend fun <T> get(
        path: String,
        map: Map<String, String>?,
        clazz: Class<T>? = null,
        type: Type? = null
    ): T
}