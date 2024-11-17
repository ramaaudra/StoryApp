package com.dicoding.picodiploma.loginwithanimation.view.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<ResultState<String>>(ResultState.Loading)
    val loginState: StateFlow<ResultState<String>> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ResultState.Loading
            try {
                val message = repository.login(email, password)
                repository.getSession().first().let { user ->
                    if (user.token.isEmpty()) {
                        _loginState.value = ResultState.Error("Login failed: Token not saved")
                    } else {
                        _loginState.value = ResultState.Success(message)
                    }
                }
            } catch (e: Exception) {
                _loginState.value = ResultState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}