package com.qlive.avparam

import java.lang.Exception

class RtcException(val code: Int, val msg: String) : Exception(msg)