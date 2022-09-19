package com.softsugar.library.sdk.entity

class DownLoadResult(
    val status: Int,
    val info: String,
    val sdPath: String
) {
    override fun toString(): String {
        return "DownLoadResult(status=$status, info='$info', sdPath='$sdPath')"
    }
}