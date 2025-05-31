package com.example.watwallet.data.repository

import android.util.Log
import com.example.watwallet.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class TransactionRepositoryImpl(
    private val jobRepository: JobRepository
) : TransactionsRepository {

    private val db = Firebase.firestore

    override suspend fun addIncome(incomeCreateModel: IncomeCreateModel): DocumentReference {
        val userRef = db.collection("users").document(incomeCreateModel.userId)
        val jobRef = userRef.collection("jobs").document(incomeCreateModel.jobId)

        val income = Income(
            job = jobRef,
            user = userRef,
            season = incomeCreateModel.season.toLong(),
            baseEarned = incomeCreateModel.baseEarned.toDouble(),
            tipsEarned = incomeCreateModel.tipsEarned.toDouble(),
            hoursWorked = incomeCreateModel.hoursWorked.toLong(),
            date = DateUtils.localDateToTimestamp(incomeCreateModel.date)
        )
        return db.collection("incomes").add(income).await()
    }

    override suspend fun addExpense(expenseCreateModel: ExpenseCreateModel): DocumentReference {
        val userRef = db.collection("users").document(expenseCreateModel.userId)

        val income = Expense(
            amount = expenseCreateModel.amount.toDouble(),
            date = DateUtils.localDateToTimestamp(expenseCreateModel.date),
            description = expenseCreateModel.description,
            season = expenseCreateModel.season.toLong(),
            tag = expenseCreateModel.tag,
            user = userRef
        )
        return db.collection("expenses").add(income).await()
    }

    override suspend fun getExpense(id: String): ExpenseGetModel? {
        return try {
            val expenseRes =
                db.collection("expenses").document(id).get().await()
            val expense = expenseRes.toObject<Expense>() ?: return null

            ExpenseGetModel(
                id = expenseRes.id,
                amount = expense.amount,
                date = DateUtils.timestampToLocalDate(expense.date),
                description = expense.description,
                season = expense.season,
                tag = expense.tag,
                userId = expense.user!!.id
            )
        } catch (e: Exception) {
            Log.e("Exc",e.message ?: "")
            null
        }
    }

    override suspend fun getIncome(id: String): IncomeGetModel? {
        return try {
            val incomeRes =
                db.collection("incomes").document(id).get().await()
            val job = incomeRes.getDocumentReference("job")?.let { job ->
                incomeRes.getDocumentReference("user")?.let { user ->
                    jobRepository.getJob(
                        user.id, job.id
                    )
                }
            }

            if (incomeRes == null || job == null) return null

            val income = incomeRes.toObject<Income>()!!

            IncomeGetModel(
                id = incomeRes.id,
                date = DateUtils.timestampToLocalDate(income.date),
                season = income.season,
                userId = income.user!!.id,
                job = job,
                baseEarned = income.baseEarned,
                tipsEarned = income.tipsEarned,
                hoursWorked = income.hoursWorked,
            )
        } catch (e: Exception) {
            Log.e("Exc",e.message ?: "")
            null
        }
    }

    override suspend fun updateExpense(expenseId: String, expense: ExpenseUpdateModel) {
        db.collection("expenses").document(expenseId).update(
            hashMapOf(
                "amount" to expense.amount,
                "date" to DateUtils.localDateToTimestamp(expense.date),
                "description" to expense.description,
                "tag" to expense.tag,
            )
        ).await()
    }

    override suspend fun updateIncome(incomeId: String, income: IncomeUpdateModel) {
        val job = db.collection("jobs").document(income.jobId)

        db.collection("incomes").document(incomeId).update(
            hashMapOf(
                "job" to job,
                "baseEarned" to income.baseEarned,
                "tipsEarned" to income.tipsEarned,
                "hoursWorked" to income.hoursWorked,
                "date" to DateUtils.localDateToTimestamp(income.date)
            )
        ).await()
    }

    override suspend fun getAllTransactions(userId: String): TotalTransactionsModel {
        // Fetch incomes and expenses
        val userRef = db.collection("users").document(userId)
        val incomesSnapshot = db.collection("incomes").whereEqualTo("user", userRef).get().await()
        val expensesSnapshot =
            db.collection("expenses").whereEqualTo("user", userRef).get().await()

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
            val job = doc.getDocumentReference("job")?.get()?.await() ?: return@mapNotNull null
            val totalAmount =
                (doc.getDouble("baseEarned") ?: 0.0) + (doc.getDouble("tipsEarned") ?: 0.0)
            val date = doc.getTimestamp("date") ?: return@mapNotNull null

            TransactionGetModel(
                uid = doc.id,
                transactionType = TransactionType.Income,
                totalAmount = totalAmount.toString(),
                date = DateUtils.timestampToLocalDate(date),
                description = "Salary from ${job.getString("position")}"
            )
        }

        val expenseTransactions = expensesSnapshot.documents.mapNotNull { doc ->
            val amount = doc.getDouble("amount") ?: return@mapNotNull null
            val date = doc.getTimestamp("date") ?: return@mapNotNull null
            val description = doc.getString("description") ?: "No Description"

            TransactionGetModel(
                uid = doc.id,
                transactionType = TransactionType.Expense,
                totalAmount = amount.toString(),
                date = DateUtils.timestampToLocalDate(date),
                description = description
            )
        }

        // Merge both lists
        val allTransactions =
            (incomeTransactions + expenseTransactions).sortedByDescending { it.date }

        // Return the final DTO
        return TotalTransactionsModel(
            earnings = totalIncomes,
            expenses = totalExpenses,
            transactions = allTransactions
        )
    }

    override suspend fun deleteIncome(incomeId: String) {
        db.collection("incomes").document(incomeId).delete().await()
    }

    override suspend fun deleteExpense(expenseId: String) {
        db.collection("expenses").document(expenseId).delete().await()
    }
}
