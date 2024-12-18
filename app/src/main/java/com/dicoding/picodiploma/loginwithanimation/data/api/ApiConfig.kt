package com.dicoding.picodiploma.loginwithanimation.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    @Volatile
    private var apiService: ApiService? = null

    fun getApiService(token: String = ""): ApiService {
        if (apiService == null || token.isNotEmpty()) {
            val loggingInterceptor = HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)

            val authInterceptor = Interceptor { chain ->
                val req = chain.request()
                val requestHeaders = if (token.isNotEmpty()) {
                    req.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    req.newBuilder().build()
                }
                Log.d("ApiConfig", "Request Headers: ${requestHeaders.headers}")
                chain.proceed(requestHeaders)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://story-api.dicoding.dev/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }

    fun updateApiService(token: String) {
        apiService = getApiService(token)
    }
}
