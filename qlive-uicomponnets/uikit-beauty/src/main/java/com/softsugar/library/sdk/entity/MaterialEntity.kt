package com.softsugar.library.sdk.entity

data class MaterialEntity(
    val id: Int,
    val name: String,
    val effectsId: String,
    val thumbnail: String,
    val pkgUrl: String,
    var zipSdPath: String
)