package com.qlive.coreimpl.model

import com.google.gson.annotations.Expose
import com.qlive.core.been.QLiveUser
import java.io.Serializable

class InnerUser : QLiveUser(), Serializable {
    @Expose(serialize = false, deserialize = false)
    var im_password: String = ""
}