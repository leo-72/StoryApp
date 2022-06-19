package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.ViewModel
import com.bangkit.storyapp.data.repository.StoryRepository

class LoginViewModel(private val storyRepository: StoryRepository): ViewModel()  {

    fun login(email: String, pass: String) =
        storyRepository.signIn(email, pass)

}