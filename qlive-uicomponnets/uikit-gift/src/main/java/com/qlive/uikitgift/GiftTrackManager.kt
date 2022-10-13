package com.qlive.uikitgift

import com.qlive.core.QLiveClient
import com.qlive.giftservice.QGiftService
import com.qlive.giftservice.QGiftServiceListener

class GiftTrackManager(val client: QLiveClient) : SpanTrackManager() {
    private val mGiftServiceLister = QGiftServiceListener {
        onNewTrackArrive(it)
    }

    init {
        client.getService(QGiftService::class.java).addGiftServiceListener(mGiftServiceLister)
    }

    fun release() {
        client.getService(QGiftService::class.java).removeGiftServiceListener(mGiftServiceLister)
    }
}