package com.dicoding.picodiploma.loginwithanimation.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.loginwithanimation.data.api.LocationRepository
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import com.dicoding.picodiploma.loginwithanimation.di.Injection
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginViewModel
import com.dicoding.picodiploma.loginwithanimation.view.main.MainViewModel
import com.dicoding.picodiploma.loginwithanimation.view.main.MapsViewModel
import com.dicoding.picodiploma.loginwithanimation.view.signup.RegisterViewModel
import com.dicoding.picodiploma.loginwithanimation.view.upload.UploadViewModel


class ViewModelFactory private constructor(
    private val repository: UserRepository,
    private val location: LocationRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(UploadViewModel::class.java) -> {
                UploadViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(location) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context,): ViewModelFactory {
            clearInstance()

            synchronized(ViewModelFactory::class.java) {
                val userRepository = Injection.provideRepository(context)
                val locationRepository = Injection.provideLocationRepository(context)
                INSTANCE = ViewModelFactory(userRepository, locationRepository)
            }

            return INSTANCE as ViewModelFactory
        }

        @JvmStatic
        private fun clearInstance() {
            INSTANCE = null
        }
    }
}