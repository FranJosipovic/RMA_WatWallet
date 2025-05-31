package com.example.watwallet.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.watwallet.data.repository.TransactionGetModel
import com.example.watwallet.data.repository.TransactionType
import com.example.watwallet.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    transaction: TransactionGetModel,
    finding: Boolean,
    deleting: Boolean,
    onDismissRequest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = { onDismissRequest() }) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth()
        ) {
            Column {
                Text(text = transaction.description)
                Text(
                    text = transaction.date
                        .toString(),
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onEdit()
                        },
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(20.dp) // Fix height to prevent jumping
                        ) {
                            if (finding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Edit")
                            }
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onDelete()
                        },
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(20.dp) // Fix height to prevent jumping
                        ) {
                            if (deleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
            Text(
                text = if (transaction.transactionType == TransactionType.Expense) "-$${transaction.totalAmount}" else "$${transaction.totalAmount}",
                color = if (transaction.transactionType == TransactionType.Expense) Color.Red else Color.Green,
                modifier = Modifier.align(Alignment.TopEnd)
            )

        }
    }
}