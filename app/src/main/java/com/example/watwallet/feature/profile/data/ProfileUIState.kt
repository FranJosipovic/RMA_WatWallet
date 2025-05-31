package com.example.watwallet.feature.profile.data

import com.example.watwallet.data.repository.EmployerGetModel
import com.example.watwallet.data.repository.User
import kotlinx.datetime.LocalDate

data class JobUIState(
    val id: String,
    val description: String,
    val employer: EmployerGetModel,
    val locationInfo: String,
    val locationLongitude: Double,
    val locationLatitude: Double,
    val position: String,
    val season: Number,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class ProfileUIState(
    val user: User?,
    val error: String?,
    val jobs: List<JobUIState>,
    val loading: Boolean,
)
