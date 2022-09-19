package com.qlive.coreimpl.http

import android.text.TextUtils
import com.qlive.core.QLiveCallBack
import com.qlive.core.QTokenGetter
import com.qlive.jsonutil.JsonUtils
import com.qlive.jsonutil.ParameterizedTypeImpl
import com.qlive.liblog.QLiveLogUtil
import org.json.JSONObject
import java.io.*
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object QLiveHttpService {
    var tokenGetter: QTokenGetter? = null

    //  var baseUrl = "http://10.200.20.28:8099"
    var baseUrl = "https://live-api.qiniu.com"
    var token = ""
    val bzCodeNoError = 0
    private val mExecutorService = ThreadPoolExecutor(
        8, 100,
        60L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>()
    )

    private fun <T> request(
        method: String,
        path: String,
        jsonString: String,
        clazz: Class<T>? = null,
        type: Type? = null
    ): T {
        val url = URL(baseUrl + path)
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        var writer: BufferedWriter? = null
        var inputStream: InputStream? = null
        var br: BufferedReader? = null
        var resultStr: String = ""// 返回结果字符串
        var resultCode = -1
        var resultMsg = ""
        QLiveLogUtil.d("QLiveHttpService", " req $method $path $jsonString")
        try {
            urlConnection.connectTimeout = 5000
            urlConnection.readTimeout = 5000
            urlConnection.useCaches = true
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            urlConnection.addRequestProperty("Connection", "Keep-Alive")
            urlConnection.setRequestProperty("Authorization", token)
            urlConnection.requestMethod = method
            if (method != "GET") {
                urlConnection.doOutput = true
            }
            //开启连接
            //  urlConnection.connect()
            if (method != "GET") {
                writer = BufferedWriter(OutputStreamWriter(urlConnection.outputStream, "UTF-8"))
                writer.write(jsonString)
                writer.flush()
            }
            resultCode = urlConnection.responseCode
            resultMsg = urlConnection.responseMessage
            if (resultCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.inputStream
                br = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val sbf = StringBuffer()
                var temp: String? = null
                while (br.readLine().also { temp = it } != null) {
                    sbf.append(temp)
                    sbf.append("\r\n")
                }
                resultStr = sbf.toString()
            }

        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                br?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            urlConnection.disconnect() // 关闭远程连接
        }
        QLiveLogUtil.d(
            "QLiveHttpService",
            " response -> $method $path $jsonString   $resultCode resultStr-> $resultStr"
        )
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
            val code = obj?.code ?: -1
            if ((code == bzCodeNoError || code == 200) && obj != null) {
                return (obj.data)
            } else {
                throw (NetBzException(
                    code,
                    obj?.message ?: resultMsg
                ))
            }
        } else {
            if (499 == resultCode) {
                tokenGetter?.getTokenInfo(object : QLiveCallBack<String> {
                    override fun onError(code: Int, msg: String?) {
                    }

                    override fun onSuccess(data: String?) {
                        token = data ?: ""
                    }
                })
            }
            throw (NetBzException(
                resultCode,
                resultMsg
            ))
        }
    }

    suspend fun <T> put(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        mExecutorService.execute {
            try {
                val resp = request("PUT", path, jsonString, clazz, type)
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
                val resp = request("POST", path, jsonString, clazz, type)
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
                val resp = request("DELETE", path, jsonString, clazz, type)
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
                val resp = request("GET", path2, "{}", clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

}