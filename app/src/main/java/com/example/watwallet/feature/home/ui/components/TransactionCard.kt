package com.example.watwallet.feature.home.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.watwallet.data.repository.GetTransactionDTO
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.utils.DateUtils


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionCard(transaction: GetTransactionDTO, onClick: () -> Unit) {
    val isExpense = transaction.transactionType == TransactionType.Expense
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
            .combinedClickable(
                onClick = {},
                onDoubleClick = {
                    onClick()
                },
                onLongClick = {
                    onClick()
                },
                onLongClickLabel = "What"
            )
    ) {
        Column {
            Text(transaction.description)
            Text(
                DateUtils.timestampToLocalDate(transaction.date)
                    .toString(),
                color = Color.Gray
            )
        }
        Text(
            text = if (isExpense) "-$${transaction.totalAmount}" else "$${transaction.totalAmount}",
            color = if (isExpense) Color.Red else Color.Green,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}