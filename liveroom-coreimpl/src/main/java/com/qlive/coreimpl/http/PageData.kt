package com.qlive.coreimpl.http

import java.io.Serializable

class PageData<T> : Serializable {
    var total_count = 0
    var page_total = 0
    var end_page = false
    var list: List<T> = ArrayList<T>()
}