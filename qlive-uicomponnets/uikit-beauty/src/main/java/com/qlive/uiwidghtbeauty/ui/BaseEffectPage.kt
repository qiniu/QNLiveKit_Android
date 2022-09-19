package com.qlive.uiwidghtbeauty.ui



interface BaseEffectPage<T> {
    fun reset()
    var onItemClick: (groupIndex: String, item: T, itemIndex: Int) -> Unit
}