package com.example.watwallet.data.repository

import com.example.watwallet.utils.DateUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.datetime.LocalDate

data class IncomeCreateModel(
    val jobId: String,
    val userId: String,
    val season: Number,
    val baseEarned: Number,
    val tipsEarned: Number,
    val hoursWorked: Number,
    val date: LocalDate
)

data class ExpenseCreateModel(
    val userId: String,
    val season: Number,
    val amount: Number,
    val tag: String,
    val description: String,
    val date: LocalDate
)

data class ExpenseUpdateModel(
    val amount: Number,
    val tag: String,
    val description: String,
    val date: LocalDate
)

data class IncomeUpdateModel(
    val jobId: String,
    val baseEarned: Number,
    val tipsEarned: Number,
    val hoursWorked: Number,
    val date: LocalDate
)

enum class TransactionType {
    Income, Expense
}

data class TransactionGetModel(
    val uid: String,
    val transactionType: TransactionType,
    val totalAmount: String,
    val date: LocalDate,
    val description: String
)

data class ExpenseGetModel(
    val id: String,
    val amount: Number,
    val date: LocalDate,
    val description: String,
    val season: Number,
    val tag: String,
    val userId: String
)

data class IncomeGetModel(
    val id: String,
    val job: JobGetModel,
    val userId: String,
    val season: Number,
    val baseEarned: Number,
    val tipsEarned: Number,
    val hoursWorked: Number,
    val date: LocalDate
)

data class Expense(
    val amount: Double = 0.0,
    val date: Timestamp = Timestamp.now(),
    val description: String = "",
    val season: Long = DateUtils.currentYear.toLong(),
    val tag: String = "",
    val user: DocumentReference? = null
)

data class Income(
    val job: DocumentReference? = null,
    val user: DocumentReference? = null,
    val season: Long = DateUtils.currentYear.toLong(),
    val baseEarned: Double = 0.0,
    val tipsEarned: Double = 0.0,
    val hoursWorked: Long = 0,
    val date: Timestamp = Timestamp.now()
)

data class TotalTransactionsModel(
    val earnings: String,
    val expenses: String,
    val transactions: List<TransactionGetModel>
)

interface TransactionsRepository {
    suspend fun addIncome(incomeCreateModel: IncomeCreateModel): DocumentReference
    suspend fun addExpense(expenseCreateModel: ExpenseCreateModel): DocumentReference
    suspend fun getExpense(id: String): ExpenseGetModel?
    suspend fun getIncome(id: String): IncomeGetModel?
    suspend fun updateExpense(expenseId: String, expense: ExpenseUpdateModel)
    suspend fun updateIncome(incomeId: String, income: IncomeUpdateModel)
    suspend fun getAllTransactions(userId: String): TotalTransactionsModel
    suspend fun deleteIncome(incomeId:String)
    suspend fun deleteExpense(expenseId:String)
}
