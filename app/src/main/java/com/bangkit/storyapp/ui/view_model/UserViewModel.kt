package com.bangkit.storyapp.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.storyapp.data.model.UserModel
import com.bangkit.storyapp.data.model.UserModelPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(private val userPref: UserModelPreferences) : ViewModel() {
    fun getUser(): Flow<UserModel> {
        return userPref.getUser()
    }

    fun logout() {
        viewModelScope.launch {
            userPref.logout()
        }
    }
}