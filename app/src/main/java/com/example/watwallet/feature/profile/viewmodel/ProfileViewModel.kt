package com.example.watwallet.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.Employer
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.Season
import com.example.watwallet.data.repository.UserRepository
import com.example.watwallet.feature.profile.data.JobUI
import com.example.watwallet.feature.profile.data.ProfileUIState
import com.example.watwallet.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                            val employer = job.employer.get().await()
                            val season = job.season.get().await()
                            JobUI(
                                id = job.id,
                                description = job.description,
                                employer = Employer(
                                    employer.id,
                                    employer.getString("name") ?: ""
                                ),
                                locationInfo = job.locationInfo,
                                location = job.location,
                                position = job.position,
                                season = Season(
                                    id = season.id,
                                    season = season.getLong("season") ?: DateUtils.currentYear,
                                    current = season.getBoolean("current") ?: true
                                ),
                                startDate = DateUtils.timestampToLocalDate(job.startDate),
                                endDate = DateUtils.timestampToLocalDate(job.endDate)
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
                _profileUIState.update { it.copy(user = user, loading = false) }
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
