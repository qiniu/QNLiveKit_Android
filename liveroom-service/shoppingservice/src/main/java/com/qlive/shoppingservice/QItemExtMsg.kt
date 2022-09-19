package com.qlive.shoppingservice

import com.qlive.shoppingservice.QItem
import com.qlive.core.been.QExtension
import java.io.Serializable

internal class QItemExtMsg:Serializable {
    var item: QItem? = null
    var extension: QExtension? = null
}