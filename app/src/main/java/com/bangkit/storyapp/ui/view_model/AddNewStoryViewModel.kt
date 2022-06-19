package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.repository.StoryRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddNewStoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun postStory(
        token: String,
        desc: RequestBody,
        imageMultipart: MultipartBody.Part,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ) = storyRepository.postStory(token, desc, imageMultipart, lat, lon)
}