package com.qlive.coreimpl.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class HearBeatResp : Serializable {
    @SerializedName(value = "live_id")
    var liveId: String = ""
    @SerializedName(value = "live_status")
    var liveStatus = 0
}