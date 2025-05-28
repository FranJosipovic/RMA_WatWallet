package com.example.watwallet.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.RegisterUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val loginResponse = authRepository.login(email, password)
            onSuccess()
        }
    }

    fun register(userRegister:RegisterUser,onSuccess: () -> Unit) {
        viewModelScope.launch {
            val loginResponse = authRepository.register(userRegister)
            onSuccess()
        }
    }
}


data class UserState(
    var authenticated: Boolean,
    var data: UserData?,
    val checkedAuth: Boolean = false
)

data class UserData(
    val uid:String,
    val email: String
)