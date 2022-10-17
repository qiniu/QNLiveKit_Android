package com.qlive.coreimpl.http

import android.text.TextUtils
import com.qlive.core.QLiveCallBack
import com.qlive.core.QTokenGetter
import java.lang.reflect.Type
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

abstract class HttpClient {
    companion object {
        lateinit var httpClient: HttpClient
    }

    // var tokenGetter: QTokenGetter? = null
    var onTokenExpiredCall: (() -> Boolean)? = null
    var baseUrl = "https://live-api.qiniu.com"//"http://10.200.20.28:8099"
    var token = ""
    private val mExecutorService = ThreadPoolExecutor(
        8, 100,
        60L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>()
    )

    internal abstract fun <T> req(
        method: String,
        path: String,
        jsonString: String,
        headers: Map<String, String>?,
        clazz: Class<T>? = null,
        type: Type? = null
    ): HttpResp<T>

    private fun <T> request(
        method: String,
        path: String,
        jsonString: String,
        headers: Map<String, String>?,
        clazz: Class<T>?,
        type: Type?
    ): T {
        var ret = req(method, path, jsonString, headers, clazz, type)
        if (ret.code == 200 || ret.code == 0) {
            return ret.data
        }
        if (ret.code == 499 || ret.code == 401) {
            if (reGetTokenInfo()) {
                ret = req(method, path, jsonString, headers, clazz, type)
            }
        }
        if (ret.code == 200 || ret.code == 0) {
            return ret.data
        }
        throw NetBzException(ret.code, ret.message)
    }

    private fun reGetTokenInfo(): Boolean {
        return onTokenExpiredCall?.invoke() ?: false
    }

    suspend fun <T> put(
        path: String,
        jsonString: String,
        clazz: Class<T>? = null,
        type: Type? = null
    ) =
        suspendCoroutine<T> { contine ->
            mExecutorService.execute {
                try {
                    val resp = request("PUT", path, jsonString, null, clazz, type)
                    contine.resume(resp)
                } catch (e: Exception) {
                    contine.resumeWithException(e)
                }
            }
        }

    suspend fun <T> post(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        mExecutorService.execute {
            try {
                val resp = request("POST", path, jsonString, null, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

    suspend fun <T> delete(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        mExecutorService.execute {
            try {
                val resp = request("DELETE", path, jsonString, null, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

    suspend fun <T> get(
        path: String,
        map: Map<String, String>?,
        clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        var params = ""
        map?.let {
            params += "?"
            it.entries.forEachIndexed { index, item ->
                params += if (index == 0) {
                    ""
                } else {
                    "&"
                } + item.key + "=" + item.value
            }
        }
        val path2 = path + params
        mExecutorService.execute {
            try {
                val resp = request("GET", path2, "{}", null, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

    suspend fun <T> get(
        path: String,
        map: Map<String, String>?,
        headers: Map<String, String>?,
        clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        var params = ""
        map?.let {
            params += "?"
            it.entries.forEachIndexed { index, item ->
                params += if (index == 0) {
                    ""
                } else {
                    "&"
                } + item.key + "=" + item.value
            }
        }
        val path2 = path + params
        mExecutorService.execute {
            try {
                val resp = request("GET", path2, "{}", headers, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }
}