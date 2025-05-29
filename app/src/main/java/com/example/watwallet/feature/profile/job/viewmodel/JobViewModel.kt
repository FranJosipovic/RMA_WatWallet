package com.example.watwallet.feature.profile.job.viewmodel

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.CreateJobDTO
import com.example.watwallet.data.repository.Employer
import com.example.watwallet.data.repository.EmployerRepository
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.JobUpdateModel
import com.example.watwallet.feature.profile.job.data.JobUIState
import com.example.watwallet.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(FlowPreview::class)
class JobViewModel(
    private val jobRepository: JobRepository,
    private val employerRepository: EmployerRepository,
    private val geocoder: Geocoder
) : ViewModel() {

    private val _jobForm = MutableStateFlow(JobUIState())
    val jobForm: StateFlow<JobUIState> = _jobForm.asStateFlow()

    private val _employerSearchField = MutableStateFlow<String>("")
    val employerSearchField: StateFlow<String> = _employerSearchField.asStateFlow()

    private val _employersSearchResults = MutableStateFlow<List<Employer>>(emptyList())
    val employersSearchResultsState: StateFlow<List<Employer>> =
        _employersSearchResults.asStateFlow()

    private val _locationSearchField = MutableStateFlow("")
    val locationSearchField: StateFlow<String> = _locationSearchField.asStateFlow()

    private val _locationSearchResults = MutableStateFlow<List<Address>>(emptyList())
    val locationSearchResults: StateFlow<List<Address>> = _locationSearchResults.asStateFlow()


    fun onJobDescriptionChange(value: String) {
        _jobForm.update { it.copy(description = value) }
    }

    fun onSelectStartDate(milliseconds: Long) {
        val instant = Instant.fromEpochMilliseconds(milliseconds)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        _jobForm.update { it.copy(startDate = localDate) }
    }

    fun onSelectEndDate(milliseconds: Long) {
        val instant = Instant.fromEpochMilliseconds(milliseconds)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        _jobForm.update { it.copy(endDate = localDate) }
    }

    init {

        viewModelScope.launch {
            val result = employerRepository.get(10)
            _employersSearchResults.update { result }
        }

        viewModelScope.launch {
            employerSearchField
                .debounce(300)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    val results = employerRepository.search(query)
                    _employersSearchResults.update { results }
                }
        }

        viewModelScope.launch {
            _locationSearchField
                .debounce(300)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocationName(
                                query,
                                10,
                                Geocoder.GeocodeListener { addresses ->
                                    _locationSearchResults.update { addresses }
                                }
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("GEOCODER", "Exception during geocoding: ${e.message}")
                        _locationSearchResults.value = emptyList()
                    }
                }
        }
    }

    fun onEmployerFieldChange(value: String) {
        _employerSearchField.update { value }
        if (value.isBlank()) {
            _employersSearchResults.value = emptyList()
        }
    }

    fun selectEmployer(employer: Employer) {
        _jobForm.update { it.copy(employer = employer) }
    }

    fun unselectEmployer() {
        _jobForm.update { it.copy(employer = null) }
    }

    fun createEmployer(employerName: String, onSuccess: (employer: Employer) -> Unit) {
        viewModelScope.launch {
            val newEmployer = employerRepository.create(employerName)
            _jobForm.update { it.copy(employer = newEmployer) }
            newEmployer?.let { onSuccess(it) }
        }
    }

    fun onJobPositionUpdate(value: String) {
        _jobForm.update { it.copy(position = value) }
    }

    fun onLocationSearchChange(value: String) {
        _locationSearchField.update { value }
    }

    fun onLocationSelect(value: Address) {
        _jobForm.update { it.copy(location = value) }
    }

    fun getJobInfo(jobId: String, onLoad: (LocalDate, LocalDate) -> Unit) {
        viewModelScope.launch {
            _jobForm.update { it.copy(loading = true) }

            val job = jobRepository.getJob(jobId)
            val employer = job.employer.get().await()

            _jobForm.update {
                it.copy(
                    employer = Employer(
                        uid = employer.id,
                        name = employer.getString("name") ?: ""
                    ),
                    location = geocoder.getFromLocationName(job.locationInfo, 1)?.first(),
                    startDate = DateUtils.timestampToLocalDate(job.startDate),
                    endDate = DateUtils.timestampToLocalDate(job.endDate),
                    position = job.position,
                    description = job.description,
                    locationInfo = job.locationInfo,
                    loading = false
                )
            }

            onLoad(_jobForm.value.startDate, _jobForm.value.endDate)
        }
    }

    fun onAddJob(onSuccess: () -> Unit) {
        viewModelScope.launch {
            jobRepository.createJob(
                userId = Firebase.auth.currentUser!!.uid,
                job = CreateJobDTO(
                    description = _jobForm.value.description,
                    position = _jobForm.value.position,
                    locationInfo = "${_jobForm.value.location?.featureName}, ${_jobForm.value.location?.countryName}",
                    location = GeoPoint(
                        _jobForm.value.location?.latitude!!,
                        _jobForm.value.location?.longitude!!
                    ),
                    employer = Firebase.firestore.collection("employers")
                        .document(_jobForm.value.employer!!.uid),
                    season = Firebase.firestore.collection("seasons")
                        .document("GpA2hWiQ3RJKZVsJjk2q"),
                    startDate = DateUtils.localDateToTimestamp(_jobForm.value.startDate),
                    endDate = DateUtils.localDateToTimestamp(_jobForm.value.endDate),
                )
            )
            onSuccess()
        }
    }

    fun onUpdateJob(jobId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            jobRepository.updateJob(
                userId = "",
                jobId = jobId,
                JobUpdateModel(
                    description = _jobForm.value.description,
                    employer = Firebase.firestore.collection("employers")
                        .document(_jobForm.value.employer!!.uid),
                    locationInfo = "${_jobForm.value.location?.featureName}, ${_jobForm.value.location?.countryName}",
                    location = GeoPoint(
                        _jobForm.value.location?.latitude!!,
                        _jobForm.value.location?.longitude!!
                    ),
                    position = _jobForm.value.position,
                    startDate = DateUtils.localDateToTimestamp(_jobForm.value.startDate),
                    endDate = DateUtils.localDateToTimestamp(_jobForm.value.endDate)
                )
            )
            onSuccess()
        }
    }
}
