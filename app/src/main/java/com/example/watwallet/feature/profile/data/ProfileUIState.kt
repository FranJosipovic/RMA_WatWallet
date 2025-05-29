package com.example.watwallet.feature.profile.data

import com.example.watwallet.data.repository.Employer
import com.example.watwallet.data.repository.Job
import com.example.watwallet.data.repository.Season
import com.example.watwallet.data.repository.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import kotlinx.datetime.LocalDate

data class JobUI(
    val id: String,
    val description: String,
    val employer: Employer,
    val locationInfo: String,
    val location: GeoPoint,
    val position: String,
    val season: Season,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class ProfileUIState(
    val user: User?,
    val error: String?,
    val jobs: List<JobUI>,
    val loading: Boolean,
)
