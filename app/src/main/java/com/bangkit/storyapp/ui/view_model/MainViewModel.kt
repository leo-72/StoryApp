package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bangkit.storyapp.data.remote.response.ListStoryItem
import com.bangkit.storyapp.data.repository.StoryRepository

class MainViewModel(
    private val storyRepository: StoryRepository,
) : ViewModel() {

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        return storyRepository.getPagingStories(token).cachedIn(viewModelScope).asLiveData()
    }
}
