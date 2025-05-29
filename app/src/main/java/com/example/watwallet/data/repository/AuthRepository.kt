package com.example.watwallet.data.repository

import com.example.watwallet.data.model.login.LoginResponse
import com.example.watwallet.data.model.login.RegisterResponse

interface AuthRepository {
    suspend fun isAuthenticated(): Boolean
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun register(user: RegisterUser): RegisterResponse
    fun logout()
}