package com.dicoding.picodiploma.loginwithanimation.view.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class UploadViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uploadState =
        MutableStateFlow<ResultState<FileUploadResponse>>(ResultState.Loading)
    val uploadState: StateFlow<ResultState<FileUploadResponse>> = _uploadState

    fun uploadImage(
        multipartBody: MultipartBody.Part,
        requestBody: RequestBody,
        lat: Double? = null,
        lon: Double? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uploadState.value = ResultState.Loading
                val response = repository.uploadImage(multipartBody, requestBody, lat, lon)
                _uploadState.value = ResultState.Success(response)
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> e.message ?: "Network error occurred"
                    else -> e.message ?: "Unknown error occurred"
                }
                _uploadState.value = ResultState.Error(errorMessage)
            }
        }
    }
}