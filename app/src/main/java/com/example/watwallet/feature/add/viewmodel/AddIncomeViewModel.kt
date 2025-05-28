package com.example.watwallet.feature.add.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.CreateIncomeDTO
import com.example.watwallet.data.repository.Job
import com.example.watwallet.data.repository.TransactionsRepository
import com.example.watwallet.data.repository.User
import com.example.watwallet.data.repository.UserRepository
import com.example.watwallet.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class ValidationResult(
    val isSuccess:Boolean,
    val message: String?
)

class IncomeFormValidator{
    fun validateJob(job:Job?):ValidationResult{
        return if(job == null){
            ValidationResult(isSuccess = false, message = "Job must be selected")
        }else{
            ValidationResult(isSuccess = true, null)
        }
    }

    fun validateBaseEarned(baseEarned: String): ValidationResult{
        val baseEarnedNum = baseEarned.toFloatOrNull()
        return if(baseEarnedNum == null){
            ValidationResult(false,"Incorrect Format")
        }else{
            ValidationResult(true,null)
        }
    }

    fun validateTipsEarned(tipsEarned: String): ValidationResult{
        val tipsEarnedNum = tipsEarned.toFloatOrNull()
        return if(tipsEarnedNum == null){
            ValidationResult(false,"Incorrect Format")
        }else{
            ValidationResult(true,null)
        }
    }

    fun validateHoursWorked(hoursWorked: String): ValidationResult{
        val hoursWorkedNum = hoursWorked.toIntOrNull()
        return if(hoursWorkedNum == null){
            ValidationResult(false,"Incorrect Format")
        }else{
            ValidationResult(true,null)
        }
    }
}

data class AddIncomeUISate(
    var job: Job? = null,
    var baseEarned: String = "",
    var tipsEarned: String = "",
    var date: LocalDate = DateUtils.currentDate,
    var totalHoursWorked: String = "",
    var jobError: String? = null,
    var baseEarnedError: String? = null,
    var tipsEarnedError: String? = null,
    var totalHoursWorkedError: String? = null
)

sealed class AddIncomeFormEvent(
){
    data class JobChanged(val job:Job): AddIncomeFormEvent()
    data class BaseEarnedChanged(val  baseEarned: String): AddIncomeFormEvent()
    data class TipsEarnedChanged(val  tipsEarned: String): AddIncomeFormEvent()
    data class TotalHoursWorkedChanged(val  totalHoursWorked: String): AddIncomeFormEvent()
    data class SelectedDateChanged(val  millis: Long): AddIncomeFormEvent()
    data object OnCancel : AddIncomeFormEvent()
    data class OnSubmit(val onSuccess:()->Unit) : AddIncomeFormEvent()
}

class AddIncomeViewModel(
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionsRepository,
    private val authRepository: AuthRepository,
    private val incomeFormValidator: IncomeFormValidator = IncomeFormValidator(),
) : ViewModel(){

    private val _state = MutableStateFlow(AddIncomeUISate())
    val state: StateFlow<AddIncomeUISate> = _state.asStateFlow()

    private lateinit var user: User

    var jobs: List<Job> = emptyList()

    init {
        viewModelScope.launch {
            val userResult = userRepository.getUser()
            if(userResult == null){
                authRepository.logout()
                userRepository.clearUserCache()
            }
            user = userResult!!
            jobs = user.userInfo.seasonJobs.map {
                it.job.job
            }
            _state.update { _state.value.copy(job = jobs.first()) }
        }
    }

    fun onEvent(event: AddIncomeFormEvent){
        when(event){
            is AddIncomeFormEvent.BaseEarnedChanged -> {
                _state.update { _state.value.copy(baseEarned = event.baseEarned) }
                if(incomeFormValidator.validateBaseEarned(_state.value.baseEarned).isSuccess && _state.value.baseEarnedError != null){
                    _state.update { _state.value.copy(baseEarnedError = null) }
                }
            }
            is AddIncomeFormEvent.JobChanged -> {
                _state.update { _state.value.copy(job = event.job) }
                if(incomeFormValidator.validateJob(_state.value.job).isSuccess && _state.value.jobError != null){
                    _state.update { _state.value.copy(jobError = null) }
                }
            }
            is AddIncomeFormEvent.TipsEarnedChanged -> {
                _state.update { _state.value.copy(tipsEarned = event.tipsEarned) }
                if(incomeFormValidator.validateTipsEarned(_state.value.tipsEarned).isSuccess && _state.value.tipsEarnedError != null){
                    _state.update { _state.value.copy(tipsEarnedError = null) }
                }
            }
            is AddIncomeFormEvent.TotalHoursWorkedChanged -> {
                _state.update { _state.value.copy(totalHoursWorked = event.totalHoursWorked) }
                if(incomeFormValidator.validateHoursWorked(_state.value.totalHoursWorked).isSuccess && _state.value.totalHoursWorkedError != null){
                    _state.update { _state.value.copy(totalHoursWorkedError = null) }
                }
            }
            is AddIncomeFormEvent.SelectedDateChanged -> {
                _state.update { _state.value.copy(date = DateUtils.millisToLocalDate(millis = event.millis)) }
            }
            is AddIncomeFormEvent.OnCancel -> {
                resetState()
            }
            is AddIncomeFormEvent.OnSubmit -> {
                addTransaction(success = event.onSuccess )
            }
        }
    }

    private fun resetState(){
        _state.update { AddIncomeUISate() }
    }

    private fun addTransaction(success:()->Unit){
        viewModelScope.launch {

            var hasError = false
            //validate form
            val jobResult = incomeFormValidator.validateJob(_state.value.job)
            if(jobResult.message != null){
                _state.update { _state.value.copy(jobError = jobResult.message) }
                hasError = true
            }

            val baseEarnedResult = incomeFormValidator.validateBaseEarned(_state.value.baseEarned)
            if(baseEarnedResult.message != null){
                _state.update { _state.value.copy(baseEarnedError = baseEarnedResult.message) }
                hasError = true
            }

            val tipsEarnedResult = incomeFormValidator.validateTipsEarned(_state.value.tipsEarned)
            if(tipsEarnedResult.message != null){
                _state.update { _state.value.copy(tipsEarnedError = tipsEarnedResult.message) }
                hasError = true
            }

            val totalHoursWorkedResult = incomeFormValidator.validateHoursWorked(_state.value.totalHoursWorked)
            if(totalHoursWorkedResult.message != null){
                _state.update { _state.value.copy(totalHoursWorkedError = totalHoursWorkedResult.message) }
                hasError = true
            }

            if(hasError) return@launch

            transactionRepository.addIncome(CreateIncomeDTO(
                jobId = _state.value.job!!.uid,
                userId = user.uid,
                seasonId = user.userInfo.seasonJobs.first { it.season.current }.season.id,
                baseEarned = _state.value.baseEarned.toFloat(),
                tipsEarned = _state.value.tipsEarned.toFloat(),
                hoursWorked = _state.value.totalHoursWorked.toInt(),
                date = DateUtils.localDateToTimestamp(_state.value.date)
            ))
            success()
            resetState()
        }
    }
}