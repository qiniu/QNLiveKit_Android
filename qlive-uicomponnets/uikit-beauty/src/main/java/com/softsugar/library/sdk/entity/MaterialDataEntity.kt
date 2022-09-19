package com.softsugar.library.sdk.entity

data class MaterialDataEntity(
    val effectsListId: Int,
    val listName: String,
    val sign: String,
    val effects: MutableList<MaterialEntity>?
)