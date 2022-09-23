package com.qlive.coreimpl

import com.qlive.avparam.RtcException
import com.qlive.coreimpl.http.NetBzException
import com.qlive.rtm.RtmException
import kotlinx.coroutines.*
import java.lang.Exception


class CoroutineScopeWrap {
    var work: (suspend CoroutineScope.() -> Unit) = {}
    var error: (e: Throwable) -> Unit = {}
    var complete: () -> Unit = {}

    fun doWork(call: suspend CoroutineScope.() -> Unit) {
        this.work = call
    }

    fun catchError(error: (e: Throwable) -> Unit) {
        this.error = error
    }

    fun onFinally(call: () -> Unit) {
        this.complete = call
    }
}

fun backGround(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
) {
    GlobalScope
        .launch(dispatcher) {
            val block = CoroutineScopeWrap()
            c.invoke(block)
            try {
                block.work.invoke(this)
            } catch (e: Exception) {
                e.printStackTrace()
                block.error.invoke(e)
            } finally {
                block.complete.invoke()
            }
        }
}
fun Throwable.getCode(): Int {
    if (this is RtmException) {
        return this.code
    }
    if (this is RtcException) {
        return this.code
    }
    if (this is NetBzException) {
        return this.code
    }
    return -1
}
