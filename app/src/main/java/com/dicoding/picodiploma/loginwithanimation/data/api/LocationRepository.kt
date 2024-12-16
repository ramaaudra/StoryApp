package com.dicoding.picodiploma.loginwithanimation.data.api

import android.util.Log
import com.dicoding.picodiploma.loginwithanimation.data.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import kotlinx.coroutines.flow.firstOrNull

class LocationRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    suspend fun getStoriesWithLocation(): List<ListStoryItem> {
        try {
            val token = userPreference.getSession().firstOrNull()?.token
            Log.d("LocationRepository", "Retrieving token for getting location: $token")
            val storyResponse = apiService.getStoriesWithLocation()
            if (token == null) {
                Log.e("LocationRepository", "Token is null")
                throw NullPointerException("Token is null")
            }
            Log.d("LocationRepository", "Token: $token")
            return storyResponse.listStory
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting location: ${e.message}", e)
            throw e
        }
    }

    companion object {
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): LocationRepository {
            return LocationRepository(apiService, userPreference)
        }
    }
}