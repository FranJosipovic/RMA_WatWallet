package com.example.watwallet.data.model.login

data class LoginResponse(
    val isSuccess: Boolean,
    val errorMessage: String?
)

data class RegisterResponse(
    val isSuccess: Boolean,
    val errorMessage: String?
)