package com.qlive.coreimpl.http
import java.lang.reflect.Type

class OKConnectionHttpClient : HttpClient() {

    
    override fun <T> req(
        method: String,
        path: String,
        jsonString: String,
        clazz: Class<T>?,
        type: Type?
    ): HttpResp<T> {
        TODO("Not yet implemented")
    }
}