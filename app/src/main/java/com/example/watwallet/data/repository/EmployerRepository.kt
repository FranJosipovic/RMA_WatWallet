package com.example.watwallet.data.repository

data class EmployerGetModel(
    val id: String,
    val name: String
)

data class EmployerCreateModel(
    val name: String
)

data class Employer(
    val name: String = ""
)

interface EmployerRepository {
    suspend fun search(searchTerm: String): List<EmployerGetModel>
    suspend fun create(employerCreateModel: EmployerCreateModel): EmployerGetModel?
    suspend fun get(count: Long?): List<EmployerGetModel>
    suspend fun get(employerId: String): EmployerGetModel?
}