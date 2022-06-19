package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.remote.response.ListStoryItem

class DetailStoryViewModel: ViewModel() {
    lateinit var storyItem: ListStoryItem

    fun setDetailStory(story: ListStoryItem) : ListStoryItem{
        storyItem = story
        return storyItem
    }
}