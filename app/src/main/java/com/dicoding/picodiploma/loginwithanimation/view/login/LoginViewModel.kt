package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.ErrorResponse
import com.dicoding.picodiploma.loginwithanimation.data.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.data.ResultState
import com.dicoding.picodiploma.loginwithanimation.data.api.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<ResultState<String>>(ResultState.Loading)
    val loginState: StateFlow<ResultState<String>> = _loginState

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = ResultState.Loading
            try {
                val message = repository.login(email, password)
                _loginState.value = ResultState.Success(message)
            }catch (e: Exception) {
                _loginState.value = ResultState.Error(e.message ?: "Unknown Error")
            }
        }
    }

}