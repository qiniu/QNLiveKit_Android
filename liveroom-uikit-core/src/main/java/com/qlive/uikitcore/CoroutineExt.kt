package com.qlive.uikitcore

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.qlive.uikitcore.CoroutineExtSetting.canUseLifecycleScope
import kotlinx.coroutines.*
import java.lang.Exception

object CoroutineExtSetting{
    var canUseLifecycleScope = true
}

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

fun LifecycleOwner.backGround(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    c: CoroutineScopeWrap.() -> Unit
): Job {
    return if (canUseLifecycleScope) {
        lifecycleScope.launch(dispatcher) {
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
    } else {
        GlobalScope.launch(dispatcher) {
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
}

//个别客户无法使用 lifecycleScope
fun LifecycleOwner.tryBackGroundWithLifecycle(
    dispatcher: MainCoroutineDispatcher = Dispatchers.Main,
    work: (suspend CoroutineScope.() -> Unit) = {}
): Job {
    return if (canUseLifecycleScope) {
        lifecycleScope.launch(dispatcher) {
            work.invoke(this)
        }
    } else {
        GlobalScope.launch(dispatcher) {
            work.invoke(this)
        }
    }
}


