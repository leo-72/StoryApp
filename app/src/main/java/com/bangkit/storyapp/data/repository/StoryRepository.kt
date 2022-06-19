package com.bangkit.storyapp.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bangkit.storyapp.data.remote.StoryRemoteMediator
import com.bangkit.storyapp.data.remote.api.ApiService
import com.bangkit.storyapp.data.remote.response.ApiResponse
import com.bangkit.storyapp.data.remote.response.ListStoryItem
import com.bangkit.storyapp.data.remote.response.LoginResult
import com.bangkit.storyapp.data.remote.response.ResultResponse
import com.bangkit.storyapp.data.room.StoryDatabase
import com.bangkit.storyapp.helper.wrapEspressoIdlingRes
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val storyDB: StoryDatabase,
    private val apiService: ApiService,
) {

    fun signIn(email: String, pass: String): LiveData<ResultResponse<LoginResult>> =
        liveData {
            emit(ResultResponse.Loading)
            try {
                val response = apiService.login(email, pass)
                if (!response.error) {
                    emit(ResultResponse.Success(response.loginResult))
                } else {
                    Log.e(TAG, "Login Fail: ${response.message}")
                    emit(ResultResponse.Error(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login Exception: ${e.message.toString()} ")
                emit(ResultResponse.Error(e.message.toString()))
            }
        }

    fun signUp(name: String, email: String, pass: String): LiveData<ResultResponse<ApiResponse>> =
        liveData {
            emit(ResultResponse.Loading)
            try {
                val response = apiService.register(name, email, pass)
                if (!response.error) {
                    emit(ResultResponse.Success(response))
                } else {
                    Log.e(TAG, "Register Fail: ${response.message}")
                    emit(ResultResponse.Error(response.message))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Register Exception: ${e.message.toString()} ")
                emit(ResultResponse.Error(e.message.toString()))
            }
        }

    fun getStoryMap(token: String): LiveData<ResultResponse<List<ListStoryItem>>> =
        liveData {
            emit(ResultResponse.Loading)
            try {
                val response = apiService.getAllStoriesLocation("Bearer $token")
                if (!response.error) {
                    emit(ResultResponse.Success(response.listStory))
                } else {
                    Log.e(TAG, "Get Story Map Fail: ${response.message}")
                    emit(ResultResponse.Error(response.message))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Get Story Map Exception: ${e.message.toString()} ")
                emit(ResultResponse.Error(e.message.toString()))
            }
        }

    fun postStory(
        token: String,
        description: RequestBody,
        imageMultipart: MultipartBody.Part,
        lat: RequestBody? = null,
        lon: RequestBody? = null
    ): LiveData<ResultResponse<ApiResponse>> = liveData {
        emit(ResultResponse.Loading)
        try {
            val response = apiService.addStories("Bearer $token", description, imageMultipart, lat, lon)
            if (!response.error) {
                emit(ResultResponse.Success(response))
            } else {
                Log.e(TAG, "PostStory Fail: ${response.message}")
                emit(ResultResponse.Error(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "PostStory Exception: ${e.message.toString()} ")
            emit(ResultResponse.Error(e.message.toString()))
        }
    }

    fun getPagingStories(token: String): Flow<PagingData<ListStoryItem>> {
        wrapEspressoIdlingRes {
            @OptIn(ExperimentalPagingApi::class)
            return Pager(
                config = PagingConfig(
                    pageSize = 5
                ),
                remoteMediator = StoryRemoteMediator(storyDB, apiService, token),
                pagingSourceFactory = {
                    storyDB.storyDao().getStory()
                }
            ).flow
        }
    }

    companion object {
        private const val TAG = "StoryRepository"
    }
}