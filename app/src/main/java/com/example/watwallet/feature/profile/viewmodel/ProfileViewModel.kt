package com.example.watwallet.feature.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.UserRepository
import com.example.watwallet.data.repository.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUIState(
    val user: User?,
    val error: String?,
    val loading: Boolean
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel(){

    private val _profileUIState = MutableStateFlow( ProfileUIState(user = null, error = null, loading = true))
    val profileUIState: StateFlow<ProfileUIState> = _profileUIState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if(user != null){
                _profileUIState.update { it.copy(user = user, loading = false) }
            }else{
                _profileUIState.update { it.copy(user = null, error = "Couldn't load user", loading = false) }
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            userRepository.clearUserCache()
            _profileUIState.update { it.copy(user = null, loading = false) }
            onSuccess()
        }
    }

    fun loadUserInfo(){
        viewModelScope.launch {
            _profileUIState.update { it.copy(loading = true) }
            val user = userRepository.loadUserData()
            if(user != null){
                _profileUIState.update { it.copy(user = user, loading = false) }
            }else{
               _profileUIState.update { it.copy(user = null, error = "Couldn't load user state", loading = false) }
            }
        }
    }

    fun deleteJob(uid:String){
        viewModelScope.launch {
            userRepository.softDeleteJob(uid)
            loadUserInfo()
        }
    }
}