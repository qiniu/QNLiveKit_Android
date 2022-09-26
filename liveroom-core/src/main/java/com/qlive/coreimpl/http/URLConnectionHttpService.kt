package com.qlive.coreimpl.http

import com.qlive.core.QLiveCallBack
import com.qlive.jsonutil.JsonUtils
import com.qlive.jsonutil.ParameterizedTypeImpl
import com.qlive.liblog.QLiveLogUtil
import org.json.JSONObject
import java.io.*
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class URLConnectionHttpService : HttpService() {

    private fun <T> request(
        method: String,
        path: String,
        jsonString: String,
        clazz: Class<T>? = null,
        type: Type? = null
    ): T {
        var ret = req(method, path, jsonString, clazz, type)
        if (ret.code == 200 || ret.code == 0) {
            return ret.data
        }
        if (ret.code == 499 || ret.code == 401) {
            if (reGetTokenInfo()) {
                ret = req(method, path, jsonString, clazz, type)
            }
        }
        if (ret.code == 200 || ret.code == 0) {
            return ret.data
        }
        throw NetBzException(ret.code, ret.message)
    }

    private fun <T> req(
        method: String,
        path: String,
        jsonString: String,
        clazz: Class<T>? = null,
        type: Type? = null
    ): HttpResp<T> {
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
                com.qlive.coreimpl.http.HttpResp::class.java,
                com.qlive.coreimpl.http.HttpResp::class.java
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

    private fun reGetTokenInfo(): Boolean {
        val latch = CountDownLatch(1)
        var reGet = false
        tokenGetter!!.getTokenInfo(object : QLiveCallBack<String> {
            override fun onError(code: Int, msg: String?) {
                reGet = false
                latch.countDown()
            }

            override fun onSuccess(data: String?) {
                reGet = true
                token = data ?: ""
                latch.countDown()
            }
        })
        latch.await()
        return reGet
    }

    override suspend fun <T> put(path: String, jsonString: String, clazz: Class<T>?, type: Type?) =
        suspendCoroutine<T> { contine ->
            mExecutorService.execute {
                try {
                    val resp = request("PUT", path, jsonString, clazz, type)
                    contine.resume(resp)
                } catch (e: Exception) {
                    contine.resumeWithException(e)
                }
            }
        }

    override suspend fun <T> post(
        path: String, jsonString: String, clazz: Class<T>?,
        type: Type?
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

    override suspend fun <T> delete(
        path: String, jsonString: String, clazz: Class<T>?,
        type: Type?
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

    override suspend fun <T> get(
        path: String,
        map: Map<String, String>?,
        clazz: Class<T>?,
        type: Type?
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