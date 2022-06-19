package com.bangkit.storyapp.data.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangkit.storyapp.data.remote.response.ListStoryItem

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(storyEntity: List<ListStoryItem>)

    @Query("SELECT * FROM story_app")
    fun getStory():  PagingSource<Int, ListStoryItem>

    @Query("DELETE FROM story_app")
    suspend fun deleteAll()
}