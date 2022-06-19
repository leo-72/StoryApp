package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.repository.StoryRepository

class RegisterViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun register(name: String, email: String, pass: String) =
        storyRepository.signUp(name,email, pass)

}