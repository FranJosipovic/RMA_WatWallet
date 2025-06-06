package com.example.watwallet.feature.profile.job.data

import android.location.Address
import com.example.watwallet.data.repository.EmployerGetModel
import com.example.watwallet.utils.DateUtils
import kotlinx.datetime.LocalDate

data class JobUIState(
    val position: String = "",
    val locationInfo: String = "",
    val location: Address? = null,
    val description: String = "",
    val startDate: LocalDate = DateUtils.currentDate,
    val endDate: LocalDate = DateUtils.currentDate,
    val employer: EmployerGetModel? = null,
    val loading: Boolean = false,
)

data class JobFormErrorState(
    val positionError: String? = null,
    val locationInfoError: String? = null,
    val descriptionError: String? = null,
    val employerError: String? = null
)