package com.example.watwallet.data.repository

interface EmployerRepository {
    suspend fun search(searchTerm:String): List<Employer>
    suspend fun create(employerName:String): Employer?
    suspend fun get(count: Long?): List<Employer>
}