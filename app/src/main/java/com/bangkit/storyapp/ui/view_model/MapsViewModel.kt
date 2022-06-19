package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.repository.StoryRepository

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun getStories(token: String) = storyRepository.getStoryMap(token)

}