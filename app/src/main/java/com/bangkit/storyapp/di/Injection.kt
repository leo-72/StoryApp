package com.bangkit.storyapp.di

import android.content.Context
import com.bangkit.storyapp.data.remote.api.ApiConfig
import com.bangkit.storyapp.data.repository.StoryRepository
import com.bangkit.storyapp.data.room.StoryDatabase

object Injection {
    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getInstance(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService)
    }
}