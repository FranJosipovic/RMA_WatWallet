package com.example.watwallet.feature.addtransaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.ExpenseCreateModel
import com.example.watwallet.data.repository.IncomeCreateModel
import com.example.watwallet.data.repository.JobGetModel
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.TransactionsRepository
import com.example.watwallet.data.repository.User
import com.example.watwallet.data.repository.UserRepository
import com.example.watwallet.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

val expenseTags = listOf("Work", "Travel", "Personal")

data class ValidationResult(
    val isSuccess: Boolean,
    val message: String?
)


data class AddExpenseUISate(
    var amount: String = "",
    var date: LocalDate = DateUtils.currentDate,
    var tag: String = expenseTags[0],
    var description: String = "",
    var amountError: String? = null,
    var descriptionError: String? = null
)

data class AddIncomeUISate(
    var job: JobGetModel? = null,
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
) {
    data class JobChanged(val job: JobGetModel) : AddIncomeFormEvent()
    data class BaseEarnedChanged(val baseEarned: String) : AddIncomeFormEvent()
    data class TipsEarnedChanged(val tipsEarned: String) : AddIncomeFormEvent()
    data class TotalHoursWorkedChanged(val totalHoursWorked: String) : AddIncomeFormEvent()
    data class SelectedDateChanged(val millis: Long) : AddIncomeFormEvent()
    data object OnCancel : AddIncomeFormEvent()
    data class OnSubmit(val onSuccess: () -> Unit) : AddIncomeFormEvent()
}

sealed class AddExpenseFormEvent(
) {
    data class AmountChanged(val amount: String) : AddExpenseFormEvent()
    data class DescriptionChanged(val description: String) : AddExpenseFormEvent()
    data class TagChanged(val tag: String) : AddExpenseFormEvent()
    data class SelectedDateChanged(val millis: Long) : AddExpenseFormEvent()
    data object OnCancel : AddExpenseFormEvent()
    data class OnSubmit(val onSuccess: () -> Unit) : AddExpenseFormEvent()
}

class ExpenseFormValidator {
    fun validateAmount(amount: String): ValidationResult {
        val baseEarnedNum = amount.toFloatOrNull()
        return if (baseEarnedNum == null) {
            ValidationResult(false, "Incorrect Format")
        } else {
            ValidationResult(true, null)
        }
    }

    fun validateDescription(description: String): ValidationResult {
        return if (description.isEmpty()) {
            ValidationResult(false, "Description cannot be empty")
        } else {
            ValidationResult(true, null)
        }
    }
}

class IncomeFormValidator {
    fun validateJob(job: JobGetModel?): ValidationResult {
        return if (job == null) {
            ValidationResult(isSuccess = false, message = "Job must be selected")
        } else {
            ValidationResult(isSuccess = true, null)
        }
    }

    fun validateBaseEarned(baseEarned: String): ValidationResult {
        val baseEarnedNum = baseEarned.toFloatOrNull()
        return if (baseEarnedNum == null) {
            ValidationResult(false, "Incorrect Format")
        } else {
            ValidationResult(true, null)
        }
    }

    fun validateTipsEarned(tipsEarned: String): ValidationResult {
        val tipsEarnedNum = tipsEarned.toFloatOrNull()
        return if (tipsEarnedNum == null) {
            ValidationResult(false, "Incorrect Format")
        } else {
            ValidationResult(true, null)
        }
    }

    fun validateHoursWorked(hoursWorked: String): ValidationResult {
        val hoursWorkedNum = hoursWorked.toIntOrNull()
        return if (hoursWorkedNum == null) {
            ValidationResult(false, "Incorrect Format")
        } else {
            ValidationResult(true, null)
        }
    }
}

var tags: List<String> = expenseTags

class AddTransactionViewModel(
    private val userRepository: UserRepository,
    private val transactionsRepository: TransactionsRepository,
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
    private val expenseFormValidator: ExpenseFormValidator = ExpenseFormValidator(),
    private val incomeFormValidator: IncomeFormValidator = IncomeFormValidator()
) : ViewModel() {

    private val _expenseState = MutableStateFlow(AddExpenseUISate())
    val expenseState: StateFlow<AddExpenseUISate> = _expenseState.asStateFlow()

    private val _incomeState = MutableStateFlow(AddIncomeUISate())
    val incomeState: StateFlow<AddIncomeUISate> = _incomeState.asStateFlow()

    private lateinit var user: User

    private val _jobs = MutableStateFlow<List<JobGetModel>>(emptyList())
    val jobs: StateFlow<List<JobGetModel>> = _jobs.asStateFlow()

    init {
        viewModelScope.launch {
            val userResult = userRepository.getUser()
            if (userResult == null) {
                authRepository.logout()
                userRepository.clearUserCache()
            }
            user = userResult!!
            _jobs.update { jobRepository.getJobs(userId = user.uid) }
        }
    }

    fun onIncomeEvent(event: AddIncomeFormEvent) {
        when (event) {
            is AddIncomeFormEvent.BaseEarnedChanged -> {
                _incomeState.update { it.copy(baseEarned = event.baseEarned) }
                if (incomeFormValidator.validateBaseEarned(_incomeState.value.baseEarned).isSuccess && _incomeState.value.baseEarnedError != null) {
                    _incomeState.update { it.copy(baseEarnedError = null) }
                }
            }

            is AddIncomeFormEvent.JobChanged -> {
                _incomeState.update { it.copy(job = event.job) }
                if (incomeFormValidator.validateJob(_incomeState.value.job).isSuccess && _incomeState.value.jobError != null) {
                    _incomeState.update { it.copy(jobError = null) }
                }
            }

            is AddIncomeFormEvent.TipsEarnedChanged -> {
                _incomeState.update { it.copy(tipsEarned = event.tipsEarned) }
                if (incomeFormValidator.validateTipsEarned(_incomeState.value.tipsEarned).isSuccess && _incomeState.value.tipsEarnedError != null) {
                    _incomeState.update { it.copy(tipsEarnedError = null) }
                }
            }

            is AddIncomeFormEvent.TotalHoursWorkedChanged -> {
                _incomeState.update { it.copy(totalHoursWorked = event.totalHoursWorked) }
                if (incomeFormValidator.validateHoursWorked(_incomeState.value.totalHoursWorked).isSuccess && _incomeState.value.totalHoursWorkedError != null) {
                    _incomeState.update { it.copy(totalHoursWorkedError = null) }
                }
            }

            is AddIncomeFormEvent.SelectedDateChanged -> {
                _incomeState.update { it.copy(date = DateUtils.millisToLocalDate(millis = event.millis)) }
            }

            is AddIncomeFormEvent.OnCancel -> {
                resetIncomeState()
            }

            is AddIncomeFormEvent.OnSubmit -> {
                addIncome(success = event.onSuccess)
            }
        }
    }

    fun onExpenseEvent(event: AddExpenseFormEvent) {
        when (event) {
            is AddExpenseFormEvent.AmountChanged -> {
                _expenseState.update { _expenseState.value.copy(amount = event.amount) }
                if (expenseFormValidator.validateAmount(event.amount).isSuccess && _expenseState.value.amountError != null) {
                    _expenseState.update { _expenseState.value.copy(amountError = null) }
                }
            }

            is AddExpenseFormEvent.DescriptionChanged -> {
                _expenseState.update { _expenseState.value.copy(description = event.description) }
                if (expenseFormValidator.validateDescription(event.description).isSuccess && _expenseState.value.descriptionError != null) {
                    _expenseState.update { _expenseState.value.copy(descriptionError = null) }
                }
            }

            AddExpenseFormEvent.OnCancel -> resetExpenseState()
            is AddExpenseFormEvent.OnSubmit -> {
                addExpense(event.onSuccess)
            }

            is AddExpenseFormEvent.SelectedDateChanged -> {
                _expenseState.update {
                    _expenseState.value.copy(
                        date = DateUtils.millisToLocalDate(
                            event.millis
                        )
                    )
                }
            }

            is AddExpenseFormEvent.TagChanged -> {
                _expenseState.update { _expenseState.value.copy(tag = event.tag) }
            }
        }
    }

    private fun resetIncomeState() {
        _incomeState.update { AddIncomeUISate() }
    }

    private fun resetExpenseState() {
        _expenseState.update { AddExpenseUISate() }
    }

    private fun addExpense(success: () -> Unit) {
        viewModelScope.launch {

            var hasError = false
            //validate form
            val amountResult = expenseFormValidator.validateAmount(_expenseState.value.amount)
            if (amountResult.message != null) {
                _expenseState.update { _expenseState.value.copy(amountError = amountResult.message) }
                hasError = true
            }

            val descriptionResult =
                expenseFormValidator.validateDescription(_expenseState.value.description)
            if (descriptionResult.message != null) {
                _expenseState.update { _expenseState.value.copy(descriptionError = descriptionResult.message) }
                hasError = true
            }

            if (hasError) return@launch

            transactionsRepository.addExpense(
                ExpenseCreateModel(
                    season = DateUtils.currentYear,
                    amount = _expenseState.value.amount.toFloat(),
                    description = _expenseState.value.description,
                    tag = _expenseState.value.tag,
                    date = _expenseState.value.date,
                    userId = user.uid
                )
            )
            success()
            resetExpenseState()
        }
    }

    private fun addIncome(success: () -> Unit) {
        viewModelScope.launch {

            var hasError = false
            //validate form
            val jobResult = incomeFormValidator.validateJob(_incomeState.value.job)
            if (jobResult.message != null) {
                _incomeState.update { it.copy(jobError = jobResult.message) }
                hasError = true
            }

            val baseEarnedResult =
                incomeFormValidator.validateBaseEarned(_incomeState.value.baseEarned)
            if (baseEarnedResult.message != null) {
                _incomeState.update { it.copy(baseEarnedError = baseEarnedResult.message) }
                hasError = true
            }

            val tipsEarnedResult =
                incomeFormValidator.validateTipsEarned(_incomeState.value.tipsEarned)
            if (tipsEarnedResult.message != null) {
                _incomeState.update { it.copy(tipsEarnedError = tipsEarnedResult.message) }
                hasError = true
            }

            val totalHoursWorkedResult =
                incomeFormValidator.validateHoursWorked(_incomeState.value.totalHoursWorked)
            if (totalHoursWorkedResult.message != null) {
                _incomeState.update { it.copy(totalHoursWorkedError = totalHoursWorkedResult.message) }
                hasError = true
            }

            if (hasError) return@launch

            transactionsRepository.addIncome(
                IncomeCreateModel(
                    jobId = _incomeState.value.job!!.id,
                    userId = user.uid,
                    season = DateUtils.currentYear,
                    baseEarned = _incomeState.value.baseEarned.toFloat(),
                    tipsEarned = _incomeState.value.tipsEarned.toFloat(),
                    hoursWorked = _incomeState.value.totalHoursWorked.toInt(),
                    date = _incomeState.value.date
                )
            )
            success()
            resetIncomeState()
        }
    }
}