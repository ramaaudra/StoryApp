package com.dicoding.picodiploma.loginwithanimation

import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem

object DataDummy {

    fun generateDummyListStoryItem(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = mutableListOf()
        for (i in 0..100) {
            val storyItem = ListStoryItem(
                "url $i",
                "created $i",
                "name $i",
                "description $i",
                i.toDouble(),
                i.toString(),
                i.toDouble()
            )
            items.add(storyItem)
        }
        return items
    }
}