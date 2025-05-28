package com.example.watwallet.feature.add.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.CreateExpenseDTO
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
import kotlinx.datetime.LocalDate
import kotlin.math.exp

val expenseTags = listOf("Work", "Travel", "Personal")

data class AddExpenseUISate(
    var amount: String = "",
    var date: LocalDate = DateUtils.currentDate,
    var tag: String = expenseTags[0],
    var description: String = "",
    var amountError: String? = null,
    var descriptionError: String? = null
)

sealed class AddExpenseFormEvent(
){
    data class AmountChanged(val amount: String): AddExpenseFormEvent()
    data class DescriptionChanged(val  description: String): AddExpenseFormEvent()
    data class TagChanged(val  tag: String): AddExpenseFormEvent()
    data class SelectedDateChanged(val  millis: Long): AddExpenseFormEvent()
    data object OnCancel : AddExpenseFormEvent()
    data class OnSubmit(val onSuccess:()->Unit) : AddExpenseFormEvent()
}

class ExpenseFormValidator{
    fun validateAmount(amount: String): ValidationResult{
        val baseEarnedNum = amount.toFloatOrNull()
        return if(baseEarnedNum == null){
            ValidationResult(false,"Incorrect Format")
        }else{
            ValidationResult(true,null)
        }
    }

    fun validateDescription(description: String): ValidationResult{
        return if(description.isEmpty()){
            ValidationResult(false,"Description cannot be empty")
        }else{
            ValidationResult(true,null)
        }
    }

}

class AddExpenseViewModel(
    private val userRepository: UserRepository,
    private val transactionsRepository: TransactionsRepository,
    private val authRepository: AuthRepository,
    private val expenseFormValidator: ExpenseFormValidator = ExpenseFormValidator()
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseUISate())
    val state: StateFlow<AddExpenseUISate> = _state.asStateFlow()
    private lateinit var user: User

    var tags: List<String> = expenseTags

    init {
        viewModelScope.launch {
            val userResult = userRepository.getUser()
            if(userResult == null){
                authRepository.logout()
                userRepository.clearUserCache()
            }
            user = userResult!!
        }
    }

    fun onEvent(event:AddExpenseFormEvent){
        when(event){
            is AddExpenseFormEvent.AmountChanged -> {
                _state.update { _state.value.copy(amount = event.amount) }
                if(expenseFormValidator.validateAmount(event.amount).isSuccess && _state.value.amountError != null){
                    _state.update { _state.value.copy(amountError = null) }
                }
            }
            is AddExpenseFormEvent.DescriptionChanged -> {
                _state.update { _state.value.copy(description = event.description) }
                if(expenseFormValidator.validateDescription(event.description).isSuccess && _state.value.descriptionError != null){
                    _state.update { _state.value.copy(descriptionError = null) }
                }
            }
            AddExpenseFormEvent.OnCancel -> resetState()
            is AddExpenseFormEvent.OnSubmit -> {
                addTransaction(event.onSuccess)
            }
            is AddExpenseFormEvent.SelectedDateChanged -> {
                _state.update { _state.value.copy(date = DateUtils.millisToLocalDate(event.millis)) }
            }
            is AddExpenseFormEvent.TagChanged -> {
                _state.update { _state.value.copy(tag = event.tag) }
            }
        }
    }

    private fun resetState(){
        _state.update { AddExpenseUISate() }
    }

    private fun addTransaction(success:()->Unit){
        viewModelScope.launch {

            var hasError = false
            //validate form
            val amountResult = expenseFormValidator.validateAmount(_state.value.amount)
            if(amountResult.message != null){
                _state.update { _state.value.copy(amountError = amountResult.message) }
                hasError = true
            }

            val descriptionResult = expenseFormValidator.validateDescription(_state.value.description)
            if(descriptionResult.message != null){
                _state.update { _state.value.copy(descriptionError = descriptionResult.message) }
                hasError = true
            }

            if(hasError) return@launch

            transactionsRepository.addExpense(
                CreateExpenseDTO(
                    userId = user.uid,
                    seasonId = user.userInfo.seasonJobs.first { it.season.current }.season.id,
                    amount = _state.value.amount.toFloat(),
                    description = _state.value.description,
                    tag = _state.value.tag,
                    date = DateUtils.localDateToTimestamp(_state.value.date)
                )
            )
            success()
            resetState()
        }
    }
}