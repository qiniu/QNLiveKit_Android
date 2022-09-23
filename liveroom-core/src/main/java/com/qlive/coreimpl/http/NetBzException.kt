package com.qlive.coreimpl.http

import java.lang.RuntimeException

class NetBzException : RuntimeException {
    var code = 0
    private constructor() {}
    constructor(detailMessage: String?) : super(detailMessage) {}
    constructor(code: Int, detailMessage: String?) : super(detailMessage) {
        this.code = code
    }
}