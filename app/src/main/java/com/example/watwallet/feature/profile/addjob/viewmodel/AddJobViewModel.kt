package com.example.watwallet.feature.profile.addjob.viewmodel

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.CreateJobModel
import com.example.watwallet.data.repository.Employer
import com.example.watwallet.data.repository.EmployerRepository
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.JobUpdateModel
import com.example.watwallet.utils.DateUtils
import com.google.firebase.firestore.GeoPoint
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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class JobManagerViewModel(
    val position:String = "",
    val locationInfo:String = "",
    val location: Address? = null,
    val description: String = "",
    val startDate: LocalDate = DateUtils.currentDate,
    val endDate: LocalDate = DateUtils.currentDate,
    val employer: Employer? = null,
    val loading: Boolean = false
)

@OptIn(FlowPreview::class)
class AddJobViewModel(
    private val jobRepository: JobRepository,
    private val employerRepository: EmployerRepository,
    private val geocoder: Geocoder
) : ViewModel() {

    private val _jobForm = MutableStateFlow(JobManagerViewModel())
    val jobForm: StateFlow<JobManagerViewModel> = _jobForm.asStateFlow()

    private val _employerSearchField = MutableStateFlow<String>("")
    val employerSearchField: StateFlow<String> = _employerSearchField.asStateFlow()

    private val _employersSearchResults = MutableStateFlow<List<Employer>>(emptyList())
    val employersSearchResultsState: StateFlow<List<Employer>> = _employersSearchResults.asStateFlow()

    private val _locationSearchField = MutableStateFlow("")
    val locationSearchField: StateFlow<String> = _locationSearchField.asStateFlow()

    private val _locationSearchResults = MutableStateFlow<List<Address>>(emptyList())
    val locationSearchResults: StateFlow<List<Address>> = _locationSearchResults.asStateFlow()


    fun onJobDescriptionChange(value: String) {
        _jobForm.update { it.copy(description = value) }
    }

    fun onSelectStartDate(milliseconds: Long) {
        // Convert milliseconds to Instant, then to LocalDate
        val instant = Instant.fromEpochMilliseconds(milliseconds)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Update the selectedStartDate
        _jobForm.update { it.copy(startDate = localDate) }
    }

    fun onSelectEndDate(milliseconds: Long) {
        // Convert milliseconds to Instant, then to LocalDate
        val instant = Instant.fromEpochMilliseconds(milliseconds)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date

        // Update the selectedStartDate
        _jobForm.update { it.copy(endDate = localDate) }
    }

    init {
        viewModelScope.launch {
            employerSearchField
                .debounce(300) // wait 300ms after last input
                .filter { it.isNotBlank() } // avoid searching empty input
                .distinctUntilChanged() // avoid duplicate searches
                .collectLatest { query ->
                    val results = employerRepository.search(query)
                    _employersSearchResults.value = results
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

    fun getJobInfo(jobId: String, onLoad:(startDate:LocalDate, endDate: LocalDate)->Unit) {
        viewModelScope.launch {
            _jobForm.update { it.copy(loading = true) }
            val job = jobRepository.getJob(jobId)

            if (job != null) {
                _jobForm.update {
                    it.copy(
                        employer = job.employer,
                        location = geocoder.getFromLocationName(job.locationInfo, 1)?.first(),
                        startDate = job.startDate,
                        endDate = job.endDate,
                        position = job.position,
                        description = job.description,
                        locationInfo = job.locationInfo,
                        loading = false
                    )
                }
                onLoad(job.startDate, job.endDate)
            }else{
                _jobForm.update { it.copy(loading = false) }
            }
        }
    }

    fun onAddJob(onSuccess: () -> Unit) {
        viewModelScope.launch {
            jobRepository.createJob(
                job = CreateJobModel(
                    description = _jobForm.value.description,
                    position = _jobForm.value.position,
                    locationInfo = "${_jobForm.value.location?.featureName}, ${_jobForm.value.location?.countryName}",
                    location = GeoPoint(
                        _jobForm.value.location?.latitude!!,
                        _jobForm.value.location?.longitude!!
                    ),
                    employerUid = _jobForm.value.employer!!.uid
                ),

                startDate = _jobForm.value.startDate,
                endDate = _jobForm.value.endDate
            )
            onSuccess()
        }
    }

    fun onUpdateJob(jobId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            jobRepository.updateJob(
                JobUpdateModel(
                    id = jobId,
                    position = _jobForm.value.position,
                    description = _jobForm.value.description,
                    locationInfo = "${_jobForm.value.location?.featureName},${_jobForm.value.location?.countryName}",
                    startDate = DateUtils.localDateToTimestamp(_jobForm.value.startDate),
                    location = GeoPoint(
                        _jobForm.value.location?.latitude!!,
                        _jobForm.value.location?.longitude!!
                    ),
                    endDate = DateUtils.localDateToTimestamp(_jobForm.value.endDate),
                    employer = _jobForm.value.employer!!
                )
            )
            onSuccess()
        }
    }
}