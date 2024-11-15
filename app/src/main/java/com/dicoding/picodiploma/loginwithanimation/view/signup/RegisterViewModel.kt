package com.dicoding.picodiploma.loginwithanimation.view.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _registerState = MutableStateFlow<ResultState<String>>(ResultState.Loading)
    val registerState: StateFlow<ResultState<String>> = _registerState

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = ResultState.Loading
            try {
                val message = userRepository.register(name, email, password)
                _registerState.value = ResultState.Success(message)
            } catch (e: Exception) {
                _registerState.value = ResultState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}