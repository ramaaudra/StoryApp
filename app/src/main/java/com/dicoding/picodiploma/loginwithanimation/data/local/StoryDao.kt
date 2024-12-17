package com.dicoding.picodiploma.loginwithanimation.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.StoryResponse


@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<StoryResponse>)

    @Query("SELECT * FROM stories")
    fun getAllStory(): PagingSource<Int, ListStoryItem>

    @Query("DELETE FROM stories")
    suspend fun deleteAll()

}