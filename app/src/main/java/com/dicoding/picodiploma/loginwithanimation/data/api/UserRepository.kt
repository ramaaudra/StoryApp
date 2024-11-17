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
    // Helper function untuk mendapatkan ApiService dengan token terbaru
    private suspend fun getApiService(): ApiService {
        val user = userPreference.getSession().first()
        val token = user.token

        if (token.isEmpty()) {
            Log.e("UserRepository", "Token is empty when trying to get API service")
            throw IllegalStateException("Token not found")
        }

        Log.d("UserRepository", "Getting API service with token format: Bearer $token")
        return ApiConfig.getApiService(token)
    }

    suspend fun getStories(): StoryResponse {
        try {
            val user = userPreference.getSession().firstOrNull()
                ?: throw NullPointerException("User session is null")

            if (user.token.isEmpty()) {
                throw IllegalStateException("Token not found")
            }

            Log.d("UserRepository", "Fetching stories with token: ${user.token}")
            return getApiService().getStories()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting stories: ${e.message}")
            throw e
        }
    }

    suspend fun getStoryById(storyId: String): DetailResponse {
        try {
            val user = userPreference.getSession().firstOrNull()
                ?: throw NullPointerException("User session is null")

            if (user.token.isEmpty()) {
                throw IllegalStateException("Token not found")
            }

            return getApiService().getStoryById(storyId)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting story by id: ${e.message}")
            throw e
        }
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        Log.d("UserRepository", "Logging out user")
        userPreference.logout()
    }

    suspend fun register(name: String, email: String, password: String): String {
        return try {
            // Untuk register, kita tidak perlu token
            val apiService = ApiConfig.getApiService("")
            val response = apiService.register(name, email, password)
            response.message ?: "Registration successful"
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            errorBody.message ?: "An error occurred during registration"
        }
    }

    suspend fun login(email: String, password: String): String {
        try {
            // Untuk login, kita tidak perlu token
            val apiService = ApiConfig.getApiService("")
            val response = apiService.login(email, password)
            Log.d("UserRepository", "Login response: $response")

            val token = response.loginResult?.token
            if (token.isNullOrEmpty()) {
                throw Exception("Token not received from server")
            }

            val user = UserModel(
                email = email,
                token = token,
                isLogin = true
            )

            Log.d("UserRepository", "Saving user session after login: $user")
            runBlocking {
                userPreference.saveSession(user)
            }


            // Verifikasi session tersimpan
            val savedUser = userPreference.getSession().first()
            Log.d("UserRepository", "Verified saved session: $savedUser")

            return response.message ?: "Login successful"
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error: ${e.message}")
            throw e
        }
    }

    suspend fun uploadImage(multipartBody: MultipartBody.Part, requestBody: RequestBody): FileUploadResponse {
        try {
            val user = userPreference.getSession().firstOrNull()
                ?: throw NullPointerException("User session is null")

            if (user.token.isEmpty()) {
                throw IllegalStateException("Token not found")
            }

            return getApiService().uploadImage(multipartBody, requestBody)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference)
            }.also { instance = it }
    }
}