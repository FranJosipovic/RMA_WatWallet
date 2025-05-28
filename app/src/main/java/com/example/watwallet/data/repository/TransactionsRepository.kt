package com.example.watwallet.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import java.lang.ref.Reference

data class CreateIncomeDTO(
    val jobId:String,
    val userId: String,
    val seasonId: String,
    val baseEarned: Number,
    val tipsEarned: Number,
    val hoursWorked: Number,
    val date: Timestamp
)

data class CreateExpenseDTO(
    val userId: String,
    val seasonId: String,
    val amount: Number,
    val tag: String,
    val description: String,
    val date: Timestamp
)

enum class TransactionType {
    Income, Expense
}

data class GetTransactionDTO(
    val uid: String,
    val transactionType: TransactionType,
    val totalAmount: String,
    val date: Timestamp,
    val description: String
)

data class Expense(
    val uid:String,
    val amount: Number,
    val date: Timestamp,
    val description: String,
    val seasonId: String,
    val tag: String,
    val userId: String
)

data class OverallTransactionsDTO(
    val earnings: String,
    val expenses: String,
    val transactions: List<GetTransactionDTO>
)

interface TransactionsRepository {
    suspend fun addIncome(incomeDTO: CreateIncomeDTO): DocumentReference
    suspend fun addExpense(expenseDTO: CreateExpenseDTO): DocumentReference
    suspend fun getExpense(id:String): Expense?
    suspend fun updateExpense(expense: Expense)
    suspend fun getAllTransactions(userId: String): OverallTransactionsDTO
    suspend fun deleteTransaction(id:String, transactionType: TransactionType)
}