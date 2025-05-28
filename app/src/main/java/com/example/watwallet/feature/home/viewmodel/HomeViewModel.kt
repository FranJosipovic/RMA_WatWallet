package com.example.watwallet.feature.home.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.GetTransactionDTO
import com.example.watwallet.data.repository.OverallTransactionsDTO
import com.example.watwallet.data.repository.TransactionsRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

data class HomeScreenUIState(
    val loading: Boolean = true,
    val transactionsInfo: OverallTransactionsDTO = OverallTransactionsDTO("","", emptyList())
)

class HomeViewModel(private val transactionsRepository: TransactionsRepository) : ViewModel(){

    private val user = Firebase.auth.currentUser!!

    var state = mutableStateOf(
        HomeScreenUIState()
    )

    init {
        loadTransactions()
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
}