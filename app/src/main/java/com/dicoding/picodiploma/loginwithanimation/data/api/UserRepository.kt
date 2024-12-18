package com.dicoding.picodiploma.loginwithanimation.data.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.picodiploma.loginwithanimation.data.DetailResponse
import com.dicoding.picodiploma.loginwithanimation.data.ErrorResponse
import com.dicoding.picodiploma.loginwithanimation.data.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.pager.StoryPagingSource
import com.dicoding.picodiploma.loginwithanimation.data.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException


class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    // Cache for ApiService with the latest token
    private var cachedApiService: ApiService? = null

    // Helper function to get ApiService with the correct token
    private suspend fun getApiService(): ApiService {
        if (cachedApiService == null) {
            val user = userPreference.getSession().firstOrNull() ?: throw Exception("User not logged in")
            if (user.token.isEmpty()) {
                throw Exception("Token is empty")
            }
            cachedApiService = ApiConfig.getApiService(user.token)
        }
        return cachedApiService!!
    }


    fun getStoryPager(): Flow<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 10,  // Number of items per page
                enablePlaceholders = false  // To improve performance, you can set this to false
            ),
            pagingSourceFactory = {
                // Create a new instance of StoryPagingSource and pass the ApiService
                StoryPagingSource(apiService)
            }
        ).flow
    }

    suspend fun getStoryById(storyId: String): DetailResponse {
        return try {
            Log.d("UserRepository", "Fetching story with ID: $storyId")
            val service = getApiService()
            service.getStoryById(storyId)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting story by id: ${e.message}")
            throw e
        }
    }

    suspend fun register(name: String, email: String, password: String): String {
        return try {
            Log.d("UserRepository", "Registering user...")
            val response = ApiConfig.getApiService("").register(name, email, password)
            response.message ?: "Registration successful"
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()?.let {
                Gson().fromJson(it, ErrorResponse::class.java)
            }
            errorBody?.message ?: "An error occurred during registration"
        }
    }

    suspend fun login(email: String, password: String): String {
        return try {
            Log.d("UserRepository", "Logging in user...")
            val response = ApiConfig.getApiService("").login(email, password)

            val token = response.loginResult?.token
                ?: throw Exception("Token not received from server")

            val user = UserModel(
                email = email,
                token = token,
                isLogin = true
            )

            Log.d("UserRepository", "Saving user session after login: $user")
            runBlocking {
                userPreference.saveSession(user)
            }

            // Update ApiService with the new token
            updateApiService(token)

            response.message ?: "Login successful"
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error: ${e.message}")
            throw e
        }
    }

    private fun updateApiService(token: String) {
        cachedApiService = ApiConfig.getApiService(token)
    }

    suspend fun uploadImage(
        multipartBody: MultipartBody.Part,
        requestBody: RequestBody,
        lat: Double? = null,
        lon: Double? = null

    ): FileUploadResponse {
        return try {
            Log.d("UserRepository", "Uploading image...")
            val service = getApiService()
            val response = service.uploadImage(
                multipartBody,
                requestBody,
                lat,
                lon
            )
            return response
        } catch (e: Exception) {
            Log.e("UserRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    suspend fun logout() {
        Log.d("UserRepository", "Logging out user")
        userPreference.logout()
        cachedApiService = null // Reset ApiService to avoid using the old token
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(userPreference: UserPreference, apiService: ApiService): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
        }
    }
}
