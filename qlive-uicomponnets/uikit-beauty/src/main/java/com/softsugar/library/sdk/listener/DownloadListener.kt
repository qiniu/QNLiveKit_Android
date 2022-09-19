package com.softsugar.library.sdk.listener

interface DownloadListener {
    fun onStart()                   // 开始下载
    fun onFail(errorInfo: String)   // 下载失败
    fun onFinish(path: String)      // 下载完成
    fun onProgress(progress: Int)   // 下载进度
}