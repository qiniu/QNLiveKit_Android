package com.qlive.uikitcore

class KitException(val code: Int, val msg: String) : Exception(msg) {
}

fun Throwable.getCode(): Int {
    if (this is KitException) {
        return code
    }
    return -1
}