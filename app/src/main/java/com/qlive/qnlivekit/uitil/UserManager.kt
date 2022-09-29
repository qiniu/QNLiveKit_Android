package com.qlive.qnlivekit.uitil

object UserManager {
    var user: BZUser? = null
        private set

    fun init() {
        user = JsonUtils.parseObject(
            SpUtil.get("UserManager").readString("BZUser") ?: "",
            BZUser::class.java
        )
    }

    fun onLogin(user: BZUser) {
        this.user = user
        SpUtil.get("UserManager").saveData("BZUser",JsonUtils.toJson(user))
    }

}