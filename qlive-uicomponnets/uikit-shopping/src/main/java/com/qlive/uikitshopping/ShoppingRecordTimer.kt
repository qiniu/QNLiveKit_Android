package com.qlive.uikitshopping

import com.qlive.shoppingservice.QItem
import kotlinx.coroutines.*

class ShoppingRecordTimer(
    private val delayTimeMillis: Long,
    val coroutineScope: CoroutineScope = GlobalScope,
    val action: suspend CoroutineScope.(Long, String) -> Unit
) {

    var itemID = ""
    var startTimer = 0L

    fun checkStart(qItem: QItem?) {
        if (qItem == null) {
            if (job != null) {
                cancel()
            }
            return
        }
        if (qItem.record?.status == QItem.RecordInfo.RECORD_STATUS_RECORDING) {
            if (qItem.itemID == itemID) {
                return
            }
            cancel()
            itemID = qItem.itemID
            startTimer = qItem.record.start
            start()
        }
    }

    private var job: Job? = null
    var isStarting: Boolean = false
        get() = job != null
        private set

    private fun start() {
        job = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                delay(delayTimeMillis)
                action(System.currentTimeMillis()/1000L - startTimer, itemID)
            }
        }
    }

    fun cancel() {
        itemID = ""
        startTimer = 0L
        job?.cancel()
        job = null
    }
}