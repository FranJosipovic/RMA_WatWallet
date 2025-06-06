package com.example.watwallet.feature.home.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.ExpenseUpdateModel
import com.example.watwallet.data.repository.IncomeUpdateModel
import com.example.watwallet.data.repository.JobGetModel
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.TotalTransactionsModel
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.data.repository.TransactionsRepository
import com.example.watwallet.feature.addtransaction.viewmodel.ValidationResult
import com.example.watwallet.feature.home.di.homeViewModelModule
import com.example.watwallet.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class HomeScreenUIState(
    val loading: Boolean = true,
    val transactionsInfo: TotalTransactionsModel = TotalTransactionsModel("", "", emptyList())
)

sealed class EditExpenseFormEvent(
) {
    data class AmountChanged(val amount: String) : EditExpenseFormEvent()
    data class DescriptionChanged(val description: String) : EditExpenseFormEvent()
    data class TagChanged(val tag: String) : EditExpenseFormEvent()
    data class SelectedDateChanged(val millis: Long) : EditExpenseFormEvent()
    data object OnCancel : EditExpenseFormEvent()
    data class OnSubmit(val onSuccess: () -> Unit) : EditExpenseFormEvent()
}

sealed class EditIncomeFormEvent(
) {
    data class JobChanged(val job: JobGetModel) : EditIncomeFormEvent()
    data class BaseEarnedChanged(val baseEarned: String) : EditIncomeFormEvent()
    data class TipsEarnedChanged(val tipsEarned: String) : EditIncomeFormEvent()
    data class TotalHoursWorkedChanged(val totalHoursWorked: String) : EditIncomeFormEvent()
    data class SelectedDateChanged(val millis: Long) : EditIncomeFormEvent()
    data object OnCancel : EditIncomeFormEvent()
    data class OnSubmit(val onSuccess: () -> Unit) : EditIncomeFormEvent()
}

data class ExpenseEditDTO(
    val uid: String,
    val amount: String,
    val date: LocalDate,
    val description: String,
    val season: Number,
    val tag: String,
    val userId: String
)

data class IncomeEditDTO(
    val id: String,
    val baseEarned: String,
    val tipsEarned: String,
    val totalHoursWorked: String,
    val date: LocalDate,
    val job: JobGetModel
)

data class IncomeEditErrorState(
    var baseEarnedError: String? = null,
    var tipsEarnedError: String? = null,
    var totalHoursWorkedError: String? = null,
)

data class ExpenseEditErrorState(
    var amountError: String? = null,
    var descriptionError: String? = null,
)

open class CommonFormValidator {
    fun validate(value: String?): ValidationResult {
        return if (value.isNullOrEmpty()) {
            ValidationResult(
                isSuccess = false,
                message = "Field cannot be empty"
            )
        } else {
            ValidationResult(
                isSuccess = true,
                message = null
            )
        }
    }
}

class HomeViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val jobRepository: JobRepository,
    private val commonFormValidator: CommonFormValidator = CommonFormValidator()
) : ViewModel() {

    private val user = Firebase.auth.currentUser!!

    private val _incomeEditErrorState = MutableStateFlow(IncomeEditErrorState())
    val incomeEditErrorState: StateFlow<IncomeEditErrorState> = _incomeEditErrorState.asStateFlow()

    private val _expenseEditErrorState = MutableStateFlow(ExpenseEditErrorState())
    val expenseEditErrorState: StateFlow<ExpenseEditErrorState> =
        _expenseEditErrorState.asStateFlow()

    private val _deleting = MutableStateFlow(false)
    val deleting: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _finding = MutableStateFlow(false)
    val finding: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _editing = MutableStateFlow(false)
    val editing: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _selectedExpense = MutableStateFlow<ExpenseEditDTO?>(null)
    val selectedExpense: StateFlow<ExpenseEditDTO?> = _selectedExpense.asStateFlow()

    private val _selectedIncome = MutableStateFlow<IncomeEditDTO?>(null)
    val selectedIncome: StateFlow<IncomeEditDTO?> = _selectedIncome.asStateFlow()

    private val _jobs = MutableStateFlow<List<JobGetModel>>(emptyList())
    val jobs: StateFlow<List<JobGetModel>> = _jobs.asStateFlow()

    var state = mutableStateOf(
        HomeScreenUIState()
    )

    init {
        viewModelScope.launch {
            _jobs.update { jobRepository.getJobs(userId = user.uid) }
        }

        loadTransactions()
    }

    fun onEditExpenseEvent(event: EditExpenseFormEvent) {
        when (event) {
            is EditExpenseFormEvent.AmountChanged -> {
                _selectedExpense.update { it!!.copy(amount = event.amount) }
                if (commonFormValidator.validate(event.amount).isSuccess && _expenseEditErrorState.value.amountError != null) {
                    _expenseEditErrorState.update { it.copy(amountError = null) }
                }
            }

            is EditExpenseFormEvent.DescriptionChanged -> {
                _selectedExpense.update { it!!.copy(description = event.description) }
                if (commonFormValidator.validate(event.description).isSuccess && _expenseEditErrorState.value.descriptionError != null) {
                    _expenseEditErrorState.update { it.copy(descriptionError = null) }
                }
            }

            is EditExpenseFormEvent.OnCancel -> unselectExpense()
            is EditExpenseFormEvent.OnSubmit -> {
                updateExpense({
                    event.onSuccess()
                })
            }

            is EditExpenseFormEvent.SelectedDateChanged -> {
                _selectedExpense.update { it!!.copy(date = DateUtils.millisToLocalDate(event.millis)) }
            }

            is EditExpenseFormEvent.TagChanged -> {
                _selectedExpense.update { it!!.copy(tag = event.tag) }
            }
        }
    }

    fun onEditIncomeEvent(event: EditIncomeFormEvent) {
        when (event) {
            is EditIncomeFormEvent.BaseEarnedChanged -> {
                _selectedIncome.update { it?.copy(baseEarned = event.baseEarned) }
                if (commonFormValidator.validate(event.baseEarned).isSuccess && _incomeEditErrorState.value.baseEarnedError != null) {
                    _incomeEditErrorState.update { it.copy(baseEarnedError = null) }
                }
            }

            is EditIncomeFormEvent.JobChanged -> {
                _selectedIncome.update { it?.copy(job = event.job) }
            }

            is EditIncomeFormEvent.TipsEarnedChanged -> {
                _selectedIncome.update { it?.copy(tipsEarned = event.tipsEarned) }
                if (commonFormValidator.validate(event.tipsEarned).isSuccess && _incomeEditErrorState.value.tipsEarnedError != null) {
                    _incomeEditErrorState.update { it.copy(tipsEarnedError = null) }
                }
            }

            is EditIncomeFormEvent.TotalHoursWorkedChanged -> {
                _selectedIncome.update { it?.copy(totalHoursWorked = event.totalHoursWorked) }
                if (commonFormValidator.validate(event.totalHoursWorked).isSuccess && _incomeEditErrorState.value.totalHoursWorkedError != null) {
                    _incomeEditErrorState.update { it.copy(totalHoursWorkedError = null) }
                }
            }

            is EditIncomeFormEvent.SelectedDateChanged -> {
                _selectedIncome.update { it?.copy(date = DateUtils.millisToLocalDate(millis = event.millis)) }
            }

            is EditIncomeFormEvent.OnCancel -> {
                unselectIncome()
            }

            is EditIncomeFormEvent.OnSubmit -> {
                updateIncome(
                    { event.onSuccess() })
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val transactionsInfoResult =
                transactionsRepository.getAllTransactions(userId = user.uid)
            state.value =
                state.value.copy(loading = false, transactionsInfo = transactionsInfoResult)
        }
    }

    suspend fun refreshTransactions() {
        val transactionsInfoResult = transactionsRepository.getAllTransactions(userId = user.uid)
        state.value = state.value.copy(transactionsInfo = transactionsInfoResult)
    }

    fun deleteTransaction(id: String, type: TransactionType, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleting.update { true }
            when (type) {
                TransactionType.Income -> transactionsRepository.deleteIncome(id)
                TransactionType.Expense -> transactionsRepository.deleteExpense(id)
            }
            refreshTransactions()
            _deleting.update { false }
            onSuccess()
        }
    }

    fun getExpense(id: String) {
        viewModelScope.launch {
            _finding.update { true }
            val expense = transactionsRepository.getExpense(id)
            if (expense != null) {
                _selectedExpense.update {
                    ExpenseEditDTO(
                        uid = expense.id,
                        season = expense.season,
                        userId = expense.userId,
                        amount = expense.amount.toString(),
                        date = expense.date,
                        description = expense.description,
                        tag = expense.tag
                    )
                }
            }
            _finding.update { false }
        }
    }

    fun getIncome(id: String) {
        viewModelScope.launch {
            _finding.update { true }
            val income = transactionsRepository.getIncome(id)
            if (income != null) {
                _selectedIncome.update {
                    IncomeEditDTO(
                        date = income.date,
                        id = id,
                        baseEarned = income.baseEarned.toString(),
                        tipsEarned = income.tipsEarned.toString(),
                        totalHoursWorked = income.hoursWorked.toString(),
                        job = income.job,
                    )
                }
            }
            _finding.update { false }
        }
    }

    private fun updateExpense(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _editing.update { true }

            if (selectedExpense.value != null) {

                var isError = false

                val expense = selectedExpense.value!!

                if (!commonFormValidator.validate(expense.amount).isSuccess) {
                    _expenseEditErrorState.update { it.copy(amountError = "Amount cannot be empty") }
                    isError = true
                }

                if (!commonFormValidator.validate(expense.description).isSuccess) {
                    _expenseEditErrorState.update { it.copy(descriptionError = "Description cannot be empty") }
                    isError = true
                }

                if (isError) {
                    _editing.update { false }
                    return@launch
                }

                transactionsRepository.updateExpense(
                    expenseId = expense.uid,
                    ExpenseUpdateModel(
                        amount = expense.amount.toFloat(),
                        tag = expense.tag,
                        description = expense.description,
                        date = expense.date
                    )
                )
                onSuccess()
            }

            _editing.update { false }
        }
    }

    private fun updateIncome(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _editing.update { true }

            if (selectedIncome.value != null) {

                val income = selectedIncome.value!!

                var isError = false

                if (!commonFormValidator.validate(income.tipsEarned).isSuccess) {
                    _incomeEditErrorState.update { it.copy(tipsEarnedError = "Tips Cannot be empty") }
                    isError = true
                }

                if (!commonFormValidator.validate(income.baseEarned).isSuccess) {
                    _incomeEditErrorState.update { it.copy(baseEarnedError = "Base Earned Cannot be empty") }
                    isError = true
                }

                if (!commonFormValidator.validate(income.totalHoursWorked).isSuccess) {
                    _incomeEditErrorState.update { it.copy(totalHoursWorkedError = "Total Hours Worked Cannot be empty") }
                    isError = true
                }

                if (isError) {
                    _editing.update { false }
                    return@launch
                }

                transactionsRepository.updateIncome(
                    incomeId = income.id,
                    IncomeUpdateModel(
                        jobId = income.job.id,
                        baseEarned = income.baseEarned.toFloat(),
                        tipsEarned = income.tipsEarned.toFloat(),
                        hoursWorked = income.totalHoursWorked.toLong(),
                        date = income.date,
                        userId = user.uid
                    )
                )
                onSuccess()
            }

            _editing.update { false }
        }
    }

    fun unselectExpense() {
        _selectedExpense.update { null }
    }

    fun unselectIncome() {
        _selectedIncome.update { null }
    }
}