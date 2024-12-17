package com.dicoding.picodiploma.loginwithanimation.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

class ListStoriesItem {
    @Entity(tableName = "stories")
    data class ListStoriesItem(
        @PrimaryKey
        @field:SerializedName("id")
        val id: String,
        val photoUrl: String?,
        val createdAt: String?,
        val name: String?,
        val description: String?,
    )
}