package com.example.watwallet.feature.home.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.Expense
import com.example.watwallet.data.repository.GetTransactionDTO
import com.example.watwallet.data.repository.OverallTransactionsDTO
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.data.repository.TransactionsRepository
import com.example.watwallet.feature.add.viewmodel.AddExpenseFormEvent
import com.example.watwallet.feature.add.viewmodel.ExpenseFormValidator
import com.example.watwallet.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class HomeScreenUIState(
    val loading: Boolean = true,
    val transactionsInfo: OverallTransactionsDTO = OverallTransactionsDTO("","", emptyList())
)

sealed class EditExpenseFormEvent(
){
    data class AmountChanged(val amount: String): EditExpenseFormEvent()
    data class DescriptionChanged(val  description: String): EditExpenseFormEvent()
    data class TagChanged(val  tag: String): EditExpenseFormEvent()
    data class SelectedDateChanged(val  millis: Long): EditExpenseFormEvent()
    data object OnCancel : EditExpenseFormEvent()
    data class OnSubmit(val onSuccess:()->Unit) : EditExpenseFormEvent()
}

data class ExpenseEditDTO(
    val uid:String,
    val amount: String,
    val date: LocalDate,
    val description: String,
    val seasonId: String,
    val tag: String,
    val userId: String
)

class HomeViewModel(private val transactionsRepository: TransactionsRepository, private val expenseFormValidator:ExpenseFormValidator = ExpenseFormValidator()) : ViewModel(){

    private val user = Firebase.auth.currentUser!!

    private val _deleting = MutableStateFlow(false)
    val deleting: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _finding = MutableStateFlow(false)
    val finding: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _editing = MutableStateFlow(false)
    val editing: StateFlow<Boolean> = _deleting.asStateFlow()

    private val _selectedExpense = MutableStateFlow<ExpenseEditDTO?>(null)
    val selectedExpense: StateFlow<ExpenseEditDTO?> = _selectedExpense.asStateFlow()

    var state = mutableStateOf(
        HomeScreenUIState()
    )

    init {
        loadTransactions()
    }

    fun onEditExpenseEvent(event: EditExpenseFormEvent){
        when(event){
            is EditExpenseFormEvent.AmountChanged -> {
                _selectedExpense.update { it!!.copy(amount = event.amount) }
//                if(expenseFormValidator.validateAmount(event.amount).isSuccess && _state.value.amountError != null){
//                    _state.update { _state.value.copy(amountError = null) }
//                }
            }
            is EditExpenseFormEvent.DescriptionChanged -> {
                _selectedExpense.update { it!!.copy(description = event.description) }
//                if(expenseFormValidator.validateDescription(event.description).isSuccess && _state.value.descriptionError != null){
//                    _state.update { _state.value.copy(descriptionError = null) }
//                }
            }
            is EditExpenseFormEvent.OnCancel -> unselectExpense()
            is EditExpenseFormEvent.OnSubmit -> {
                updateExpense()
                event.onSuccess()
            }
            is EditExpenseFormEvent.SelectedDateChanged -> {
                _selectedExpense.update { it!!.copy(date = DateUtils.millisToLocalDate(event.millis)) }
            }
            is EditExpenseFormEvent.TagChanged -> {
                _selectedExpense.update { it!!.copy(tag = event.tag) }
            }
        }
    }

    private fun loadTransactions(){
        viewModelScope.launch {
            val transactionsInfoResult = transactionsRepository.getAllTransactions(userId = user.uid)
            state.value = state.value.copy(loading = false, transactionsInfo = transactionsInfoResult)
        }
    }

    suspend fun refreshTransactions(){
        val transactionsInfoResult = transactionsRepository.getAllTransactions(userId = user.uid)
        state.value = state.value.copy(transactionsInfo = transactionsInfoResult)
    }

    fun deleteTransaction(id:String, type:TransactionType,onSuccess:()->Unit){
        viewModelScope.launch {
            _deleting.update { true }
            transactionsRepository.deleteTransaction(id, type)
            refreshTransactions()
            _deleting.update { false }
            onSuccess()
        }
    }

    fun getExpense(id:String){
        viewModelScope.launch {
            _finding.update { true }
            val expense = transactionsRepository.getExpense(id)
            if(expense != null) {
                _selectedExpense.update {
                    ExpenseEditDTO(
                        uid = expense.uid,
                        seasonId = expense.seasonId,
                        userId = expense.userId,
                        amount = expense.amount.toString(),
                        date = DateUtils.timestampToLocalDate(expense.date),
                        description = expense.description,
                        tag = expense.tag
                    )
                }
            }
            _finding.update { false }
        }
    }

    private fun updateExpense(){
        viewModelScope.launch {
            _editing.update { true }
            transactionsRepository.updateExpense(
                Expense(
                    uid = _selectedExpense.value!!.uid,
                    amount = _selectedExpense.value!!.amount.toFloat(),
                    date = DateUtils.localDateToTimestamp(_selectedExpense.value!!.date),
                    description = _selectedExpense.value!!.description,
                    seasonId = _selectedExpense.value!!.seasonId,
                    userId = _selectedExpense.value!!.userId,
                    tag = _selectedExpense.value!!.tag
                )
            )
            _editing.update { false }
        }
    }

    fun unselectExpense(){
        _selectedExpense.update { null }
    }
}