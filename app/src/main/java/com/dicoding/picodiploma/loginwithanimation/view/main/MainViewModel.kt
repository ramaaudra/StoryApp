package com.dicoding.picodiploma.loginwithanimation.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.dicoding.picodiploma.loginwithanimation.data.DetailResponse
import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _stories = MutableLiveData<ResultState<StoryResponse>>()
    val stories: LiveData<ResultState<StoryResponse>> = _stories

    private val _story = MutableLiveData<ResultState<DetailResponse>>()
    val story: LiveData<ResultState<DetailResponse>> = _story

    private val _storyResultState = MutableLiveData<ResultState<PagingData<ListStoryItem>>>()
    val storyResultState: LiveData<ResultState<PagingData<ListStoryItem>>> = _storyResultState

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }


//    fun getStories() {
//        viewModelScope.launch {
//            try {
//                _stories.value = ResultState.Loading
//                val response = repository.getStories()
//                _stories.value = ResultState.Success(response)
//            } catch (e: Exception) {
//                android.util.Log.e("MainViewModel", "Error: ${e.message}", e)
//                _stories.value = ResultState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }

    fun getStoryById(storyId: String) {
        viewModelScope.launch {
            try {
                _story.value = ResultState.Loading
                val response = repository.getStoryById(storyId)
                _story.value = ResultState.Success(response)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error: ${e.message}", e)
                _story.value = ResultState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getStoryPager(): Flow<PagingData<ListStoryItem>> {
        return repository.getStoryPager()
    }
}