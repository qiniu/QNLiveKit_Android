package com.softsugar.library.sdk.utils

import android.content.Context
import com.softsugar.library.sdk.entity.DownLoadResult
import com.softsugar.library.sdk.listener.DownloadListener
import com.softsugar.library.sdk.net.APIInterface
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object DownloadUtils {

    const val CODE_FAIL = -1
    const val CODE_OK = 0

    fun isDownLoad(context: Context, fileName: String): Boolean {
        val path =
            context.externalCacheDir?.absolutePath + File.separator + fileName
        return File(path).exists()
    }

    fun downloadSuspend(url: String, path: String, listener: DownloadListener) {
        download(url, path, listener)
    }

    suspend fun downloadSuspend(url: String, path: String) =
        suspendCoroutine<DownLoadResult> {
            download(url, path, object : DownloadListener {
                override fun onStart() {}

                override fun onFail(errorInfo: String) {
                    it.resume(DownLoadResult(CODE_FAIL, "网络异常", ""))
                }

                override fun onFinish(path: String) {
                    it.resume(DownLoadResult(CODE_OK, "下载成功", path))
                }

                override fun onProgress(progress: Int) {}
            })
        }

    fun download(url: String, path: String, listener: DownloadListener) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://www.xxx.com") //通过线程池获取一个线程，指定callback在子线程中运行。
            .client(getOkHttpClient())
            .callbackExecutor(Executors.newSingleThreadExecutor())
            .build()

        val service = retrofit.create(APIInterface::class.java)

        val call: Call<ResponseBody> = service.download(url)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                writeResponseToDisk(path, response, listener)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                listener.onFail("网络错误~")
            }
        })
    }

    private fun getOkHttpClient(): OkHttpClient? {
        return OkHttpClient.Builder()
            .addInterceptor(getHttpLoggingInterceptor())
            .build()
    }

    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return logging
    }

    private fun writeResponseToDisk(
        path: String,
        response: Response<ResponseBody>,
        listener: DownloadListener
    ) {
        val body = response.body()
        body?.apply {
            writeFileFromIS(
                File(path),
                this.byteStream(),
                this.contentLength(),
                listener
            )
        }
        if (body == null) listener.onFail("fail")
    }

    private const val sBufferSize = 8192
    private fun writeFileFromIS(
        file: File,
        ism: InputStream,
        totalLength: Long,
        listener: DownloadListener
    ) {
        listener.onStart()

        // 创建文件
        if (!file.exists()) {
            if ((file.parentFile?.exists()) == false) {
                file.parentFile?.mkdir()
            }
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        var os: OutputStream? = null
        var currentLength: Long = 0
        try {
            os = BufferedOutputStream(FileOutputStream(file))
            val data = ByteArray(sBufferSize)
            var len: Int
            while (ism.read(data, 0, sBufferSize).also { len = it } != -1) {
                os.write(data, 0, len)
                currentLength += len.toLong()
                //计算当前下载进度
                listener.onProgress((100 * currentLength / totalLength).toInt())
            }
            os.flush()
            //下载完成，并返回保存的文件路径
            listener.onFinish(file.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            listener.onFail("IOException")
        } finally {
            try {
                ism.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}