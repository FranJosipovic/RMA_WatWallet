package com.example.watwallet.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.UserRepository
import com.example.watwallet.feature.profile.data.JobUIState
import com.example.watwallet.feature.profile.data.ProfileUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val jobsRepository: JobRepository
) : ViewModel() {

    private val _profileUIState =
        MutableStateFlow(
            ProfileUIState(
                user = null,
                error = null,
                jobs = emptyList(),
                loading = true
            )
        )
    val profileUIState: StateFlow<ProfileUIState> = _profileUIState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user != null) {
                val jobs = jobsRepository.getJobs(user.uid)
                _profileUIState.update {
                    it.copy(
                        user = user,
                        jobs = jobs.map { job ->
                            JobUIState(
                                id = job.id,
                                description = job.description,
                                employer = job.employer,
                                locationInfo = job.locationInfo,
                                position = job.position,
                                season = job.season,
                                startDate = job.startDate,
                                endDate = job.endDate,
                                locationLongitude = job.locationLongitude,
                                locationLatitude = job.locationLatitude,
                            )
                        },
                        loading = false
                    )
                }
            } else {
                _profileUIState.update {
                    it.copy(
                        error = "Couldn't load user",
                        loading = false
                    )
                }
                userRepository.clearUserCache()
                authRepository.logout()
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

    fun loadUserInfo() {
        viewModelScope.launch {
            _profileUIState.update { it.copy(loading = true) }
            val user = userRepository.loadUserData()
            if (user != null) {
                val jobs = jobsRepository.getJobs(userId = user.uid)
                _profileUIState.update {
                    it.copy(user = user, loading = false, jobs = jobs.map { job ->
                        JobUIState(
                            id = job.id,
                            description = job.description,
                            employer = job.employer,
                            locationInfo = job.locationInfo,
                            position = job.position,
                            season = job.season,
                            startDate = job.startDate,
                            endDate = job.endDate,
                            locationLongitude = job.locationLongitude,
                            locationLatitude = job.locationLatitude,
                        )
                    })
                }
            } else {
                _profileUIState.update {
                    it.copy(
                        user = null,
                        error = "Couldn't load user state",
                        loading = false
                    )
                }
            }
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            jobsRepository.deleteJob(userId = _profileUIState.value.user!!.uid, jobId = jobId)
            loadUserInfo()
        }
    }
}
