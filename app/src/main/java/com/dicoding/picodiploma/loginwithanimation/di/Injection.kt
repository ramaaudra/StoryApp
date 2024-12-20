package com.dicoding.picodiploma.loginwithanimation.di

import android.content.Context
import android.util.Log
import com.dicoding.picodiploma.loginwithanimation.data.api.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.data.api.LocationRepository
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        Log.d("Injection", "provideRepository: ${runBlocking { pref.getSession().first().token }}")
        return UserRepository.getInstance(pref, apiService)
    }

    fun provideLocationRepository(context: Context): LocationRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService(pref)
        return LocationRepository.getInstance(apiService, pref)
    }
}
