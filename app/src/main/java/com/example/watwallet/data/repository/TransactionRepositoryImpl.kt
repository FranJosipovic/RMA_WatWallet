package com.example.watwallet.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class TransactionRepositoryImpl : TransactionsRepository {

    private  val firestore = Firebase.firestore

    override suspend fun addIncome(incomeDTO: CreateIncomeDTO): DocumentReference {
        return firestore.collection("incomes").add(incomeDTO).await()
    }

    override suspend fun addExpense(expenseDTO: CreateExpenseDTO): DocumentReference {
        return firestore.collection("expenses").add(expenseDTO).await()
    }

    override suspend fun getAllTransactions(userId: String): OverallTransactionsDTO {
        // Fetch incomes and expenses
        val incomesSnapshot = firestore.collection("incomes").whereEqualTo("userId", userId).get().await()
        val expensesSnapshot = firestore.collection("expenses").whereEqualTo("userId", userId).get().await()

        // Calculate total incomes
        val totalIncomes = incomesSnapshot.documents.sumOf { doc ->
            (doc.getDouble("baseEarned") ?: 0.0) + (doc.getDouble("tipsEarned") ?: 0.0)
        }.toString()

        // Calculate total expenses
        val totalExpenses = expensesSnapshot.documents.sumOf { doc ->
            doc.getDouble("amount") ?: 0.0
        }.toString()

        // Prepare transaction lists
        val incomeTransactions = incomesSnapshot.documents.mapNotNull { doc ->
            val jobId = doc.getString("jobId") ?: return@mapNotNull null
            val totalAmount = (doc.getDouble("baseEarned") ?: 0.0) + (doc.getDouble("tipsEarned") ?: 0.0)
            val date = doc.getTimestamp("date") ?: return@mapNotNull null

            // Fetch job name from jobs collection (assuming it exists)
            val jobName = firestore.collection("jobs").document(jobId).get().await().getString("position") ?: "Unknown Job"

            GetTransactionDTO(
                uid = doc.id,
                transactionType = TransactionType.Income,
                totalAmount = totalAmount.toString(),
                date = date,
                description = "Salary from $jobName"
            )
        }

        val expenseTransactions = expensesSnapshot.documents.mapNotNull { doc ->
            val amount = doc.getDouble("amount") ?: return@mapNotNull null
            val date = doc.getTimestamp("date") ?: return@mapNotNull null
            val description = doc.getString("description") ?: "No Description"

            GetTransactionDTO(
                uid = doc.id,
                transactionType = TransactionType.Expense,
                totalAmount = amount.toString(),
                date = date,
                description = description
            )
        }

        // Merge both lists
        val allTransactions = (incomeTransactions + expenseTransactions).sortedByDescending { it.date }

        // Return the final DTO
        return OverallTransactionsDTO(
            earnings = totalIncomes,
            expenses = totalExpenses,
            transactions = allTransactions
        )
    }

}