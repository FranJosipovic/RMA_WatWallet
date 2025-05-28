package com.example.watwallet.data.remote

import com.example.watwallet.data.model.istokenvalid.IsTokenValidRequest
import com.example.watwallet.data.model.istokenvalid.IsTokenValidResponse
import com.example.watwallet.data.model.login.LoginRequest
import com.example.watwallet.data.model.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService{
    @POST("/token-valid")
    suspend fun isTokenValid(@Body body: IsTokenValidRequest): Response<IsTokenValidResponse>

    @POST("/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>
}



