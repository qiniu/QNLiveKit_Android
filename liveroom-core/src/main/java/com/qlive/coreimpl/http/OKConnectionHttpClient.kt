package com.qlive.coreimpl.http

import android.content.Context
import android.text.TextUtils
import com.qlive.jsonutil.JsonUtils
import com.qlive.jsonutil.ParameterizedTypeImpl
import com.qlive.liblog.QLiveLogUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

const val header_cache_name = "Cache-Time"

class OKConnectionHttpClient(context: Context) : HttpClient() {

    class CacheInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            val response: Response = chain.proceed(request)
            val cache = request.header(header_cache_name)
            return if (!TextUtils.isEmpty(cache)) { //缓存时间不为空
                response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control") //cache for cache seconds
                    .header("Cache-Control", "max-age=$cache")
                    .build()
            } else {
                response
            }
        }
    }

    class CacheProvide(context: Context) {
        var mContext: Context

        init {
            mContext = context
        }

        fun provideCache(): Cache { //使用应用缓存文件路径，缓存大小为10MB
            return Cache(mContext.cacheDir, 10240 * 1024)
        }
    }

    private var okHttp: OkHttpClient = OkHttpClient.Builder().connectTimeout(8000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .addNetworkInterceptor(CacheInterceptor())//缓存拦截器
        .cache(CacheProvide(context).provideCache())//缓存空间提供器
        .build()
        private set

    override fun <T> req(
        method: String,
        path: String,
        jsonString: String,
        headers: Map<String, String>?,
        clazz: Class<T>?,
        type: Type?
    ): HttpResp<T> {
        val url = baseUrl + path
        val requestBuild = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json; charset=UTF-8")
            .addHeader("Connection", "Keep-Alive")
            .addHeader("Authorization", token)
        headers?.entries?.forEach {
            requestBuild.addHeader(it.key,it.value)
        }
        val body = jsonString.toRequestBody("application/json;charset=utf-8".toMediaType())
        val req = when (method) {
            "GET" -> {
                requestBuild.get().build()
            }
            "POST" -> {
                requestBuild.post(body).build()
            }
            "DELETE" -> {
                requestBuild.delete(body).build()
            }
            "PUT" -> {
                requestBuild.put(body).build()
            }
            else -> {
                throw java.lang.Exception("Unknown request method")
            }
        }

        val response = okHttp.newCall(req).execute()
        val resultCode = response.code
        val resultMsg = response.message
        var resultStr: String = response.body?.string() ?: ""
        if (resultCode == HttpURLConnection.HTTP_OK) {
            val jsonObj = JSONObject(resultStr)
            //旧版本服务端搞错一个字段
            if (method == "GET" && path.startsWith("/client/user/users")) {
                QLiveLogUtil.d(
                    "QLiveHttpService",
                    "let Data - > data"
                )
                val data = jsonObj.optJSONArray("Data")
                if (data != null) {
                    jsonObj.put("data", data)
                    resultStr = jsonObj.toString()
                }
            }
            val p = ParameterizedTypeImpl(
                arrayOf(clazz ?: type),
                HttpResp::class.java,
                HttpResp::class.java
            )
            val obj = JsonUtils.parseObject<HttpResp<T>>(resultStr, p)
            return obj!!
        } else {
            return HttpResp<T>().apply {
                code = resultCode
                message = resultMsg
            }
        }
    }

}