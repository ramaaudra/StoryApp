package com.dicoding.picodiploma.loginwithanimation.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _stories = MutableLiveData<ResultState<StoryResponse>>()
    val stories: LiveData<ResultState<StoryResponse>> = _stories

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }


    fun getStories() {
        viewModelScope.launch {
            try {
                _stories.value = ResultState.Loading
                val response = repository.getStories()
                _stories.value = ResultState.Success(response)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error: ${e.message}", e)
                _stories.value = ResultState.Error(e.message ?: "Unknown error")
            }
        }
    }

}