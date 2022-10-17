package com.qlive.coreimpl.http

import com.qlive.jsonutil.JsonUtils
import com.qlive.jsonutil.ParameterizedTypeImpl
import com.qlive.liblog.QLiveLogUtil
import org.json.JSONObject
import java.io.*
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL

internal class URLConnectionHttpClient : HttpClient() {

    override fun <T> req(
        method: String,
        path: String,
        jsonString: String,
        headers: Map<String, String>?,
        clazz: Class<T>?,
        type: Type?
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
            headers?.entries?.forEach {
                urlConnection.setRequestProperty(it.key, it.value)
            }
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

}