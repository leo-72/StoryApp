package com.bangkit.storyapp.data.remote.response

sealed class ResultResponse<out R> private constructor() {
    object Loading : ResultResponse<Nothing>()
    data class Success<out T>(val data: T) : ResultResponse<T>()
    data class Error(val error: String) : ResultResponse<Nothing>()
}