package com.qlive.uikitcore

import kotlinx.coroutines.*


open class Scheduler(
    private val delayTimeMillis: Long,
    private val coroutineScope: CoroutineScope = GlobalScope,
    val action: suspend CoroutineScope.() -> Unit
) {
    protected var job: Job? = null
    var isStarting: Boolean = false
        get() = job != null
        private set

    fun start(delayBefore: Boolean = false) {
        job = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                if (delayBefore) {
                    delay(delayTimeMillis)
                    action()
                } else {
                    action()
                    delay(delayTimeMillis)
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}