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
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun getStories(): StoryResponse {
        return try {
            val user = userPreference.getSession().firstOrNull()
                ?: throw NullPointerException("User session is null")

            if (user.token.isEmpty()) {
                throw IllegalStateException("Token not found")
            }

            Log.d("UserRepository", "Fetching stories with token: ${user.token}")
            apiService.getStories()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting stories: ${e.message}")
            throw e
        }
    }

    suspend fun getStoryById(storyId: String): DetailResponse {
        return try {
            val user = userPreference.getSession().firstOrNull()
                ?: throw NullPointerException("User session is null")

            if (user.token.isEmpty()) {
                throw IllegalStateException("Token not found")
            }

            apiService.getStoryById(storyId)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting story by id: ${e.message}")
            throw e
        }
    }

    suspend fun register(name: String, email: String, password: String): String {
        return try {
            val response = apiService.register(name, email, password)
            response.message ?: "Registration successful"
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            errorBody.message ?: "An error occurred during registration"
        }
    }

    suspend fun login(email: String, password: String): String {
        return try {
            val response = apiService.login(email, password)
            Log.d("UserRepository", "Login response: $response")

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
            // Verifikasi session tersimpan
            val savedUser = userPreference.getSession().first()
            Log.d("UserRepository", "Verified saved session: $savedUser")

            response.message ?: "Login successful"
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error: ${e.message}")
            throw e
        }
    }

    suspend fun uploadImage(multipartBody: MultipartBody.Part, requestBody: RequestBody): FileUploadResponse {
        return try {
            val user = userPreference.getSession().firstOrNull()
                ?: throw NullPointerException("User session is null")

            if (user.token.isEmpty()) {
                throw IllegalStateException("Token not found")
            }

            apiService.uploadImage(multipartBody, requestBody)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    suspend fun logout() {
        Log.d("UserRepository", "Logging out user")
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
        }
    }
}
