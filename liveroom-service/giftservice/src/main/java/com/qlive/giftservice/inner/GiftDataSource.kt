package com.qlive.giftservice.inner

import com.google.gson.JsonObject
import com.qlive.coreimpl.http.HttpService
import com.qlive.coreimpl.http.PageData
import com.qlive.giftservice.QGift
import com.qlive.giftservice.QGiftStatistics
import com.qlive.jsonutil.ParameterizedTypeImpl

class GiftDataSource {

    suspend fun giftList(type: Int): List<QGift> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGift::class.java),
            List::class.java,
            List::class.java
        )
        return HttpService.httpClient.get("/server/gift/config/${type}", null, null, p)
    }

    suspend fun giftByID(giftID: Int): QGift {
        val gift: QGift = giftList(0).first {
            it.giftID == giftID
        }
        return gift
    }

    suspend fun roomGiftStatistics(
        liveID: String,
        page_num: Int,
        page_size: Int
    ): List<QGiftStatistics> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGiftStatistics::class.java),
            PageData::class.java,
            PageData::class.java
        )
        return HttpService.httpClient.get<PageData<QGiftStatistics>>(
            "/server/gift/live/${liveID}",
            HashMap<String, String>().apply {
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        ).list
    }

    suspend fun sendGift(live_id: String, gift_id: Int, amount: Int, redo: Boolean) {
        HttpService.httpClient.put("/client/gift/live/${live_id}", JsonObject().apply {
            addProperty("gift_id", gift_id)
            addProperty("amount", amount)
            addProperty("redo", redo)
        }.toString(), Any::class.java)
    }

    suspend fun anchorGiftStatistics(page_num: Int, page_size: Int): List<QGiftStatistics> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGiftStatistics::class.java),
            PageData::class.java,
            PageData::class.java
        )
        return HttpService.httpClient.get<PageData<QGiftStatistics>>(
            "/client/gift/anchor",
            HashMap<String, String>().apply {
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        ).list
    }

    suspend fun userGiftStatistics(page_num: Int, page_size: Int): List<QGiftStatistics> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGiftStatistics::class.java),
            PageData::class.java,
            PageData::class.java
        )
        return HttpService.httpClient.get<PageData<QGiftStatistics>>(
            "/client/gift/user",
            HashMap<String, String>().apply {
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        ).list
    }
}