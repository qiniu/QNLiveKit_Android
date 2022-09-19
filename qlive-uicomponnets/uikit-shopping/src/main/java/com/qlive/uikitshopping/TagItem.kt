package com.qlive.uikitshopping

class TagItem {
    var tagStr = ""
    var color = 0

    companion object {
        fun strToTagItem(tagStr: String): List<TagItem> {
            val tags = ArrayList<TagItem>()
            tagStr.split(",").forEachIndexed { index, s ->
                val item = TagItem()
                item.tagStr = s
                when (index) {
                    0 -> item.color = R.drawable.shape_fa8c15_fbad14_1
                    1 -> item.color = R.drawable.shape_ef4149_1
                    else -> item.color = R.drawable.shape_fa8c15_fbad14_1
                }
                tags.add(item)
            }
            return tags
        }
    }
}