package com.dicoding.picodiploma.loginwithanimation.data.api

import android.util.Log
import com.dicoding.picodiploma.loginwithanimation.data.DetailResponse
import com.dicoding.picodiploma.loginwithanimation.data.ErrorResponse
import com.dicoding.picodiploma.loginwithanimation.data.FileUploadResponse
import com.dicoding.picodiploma.loginwithanimation.data.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.data.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.StoryResponse
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException


class UserRepository private constructor(
    private val userPreference: UserPreference
) {
    // Cache untuk ApiService dengan token terbaru
    private var apiService: ApiService? = null

    // Helper function untuk mendapatkan ApiService dengan token yang benar
    private suspend fun getApiService(): ApiService {
        if (apiService == null) {
            val user = userPreference.getSession().first()
            apiService = ApiConfig.getApiService(user.token)
        }
        return apiService!!
    }

    suspend fun getStories(): StoryResponse {
        return try {
            Log.d("UserRepository", "Fetching stories...")
            val service = getApiService()
            service.getStories()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting stories: ${e.message}")
            throw e
        }
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

            // Perbarui ApiService dengan token baru
            updateApiService(token)

            response.message ?: "Login successful"
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error: ${e.message}")
            throw e
        }
    }

    private fun updateApiService(token: String) {
        apiService = ApiConfig.getApiService(token)
    }

    suspend fun uploadImage(
        multipartBody: MultipartBody.Part,
        requestBody: RequestBody
    ): FileUploadResponse {
        return try {
            Log.d("UserRepository", "Uploading image...")
            val service = getApiService()
            service.uploadImage(multipartBody, requestBody)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    suspend fun logout() {
        Log.d("UserRepository", "Logging out user")
        userPreference.logout()
        apiService = null // Reset ApiService agar tidak menggunakan token lama
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(userPreference: UserPreference): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference)
            }.also { instance = it }
        }
    }
}
