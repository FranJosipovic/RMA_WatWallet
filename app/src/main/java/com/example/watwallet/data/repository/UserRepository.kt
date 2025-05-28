package com.example.watwallet.data.repository

import com.example.watwallet.data.model.login.LoginResponse

data class RegisterUser(
    val email: String,
    val password: String,
    val name:String,
    val surname:String,
    val phone:String
)

interface UserRepository {
    suspend fun getUser():User?
    suspend fun loadUserData(): User?
    suspend fun updateUserInfo(): User?
    fun clearUserCache()
    suspend fun softDeleteJob(uid:String)
    suspend fun updateSeasonJobs(seasonJobs:List<SeasonJob>)
}