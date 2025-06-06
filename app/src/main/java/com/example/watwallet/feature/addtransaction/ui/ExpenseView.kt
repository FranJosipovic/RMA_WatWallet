package com.example.watwallet.feature.addtransaction.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watwallet.feature.addtransaction.viewmodel.AddExpenseFormEvent
import com.example.watwallet.feature.addtransaction.viewmodel.AddTransactionViewModel
import com.example.watwallet.feature.addtransaction.viewmodel.tags
import com.example.watwallet.ui.components.CustomDatePickerDialog
import com.example.watwallet.ui.components.MoneyInputField
import kotlinx.coroutines.launch

@Composable
fun AddExpenseView(
    snackbarHostState: SnackbarHostState,
    addTransactionViewModel: AddTransactionViewModel,
    onExpenseAdded: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val state by addTransactionViewModel.expenseState.collectAsState()

    var openDatePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    CustomDatePickerDialog(
        show = openDatePicker,
        selectedStartDate = state.date,
        onDismissRequest = {openDatePicker = false },
        onSelectDate = {
            addTransactionViewModel.onExpenseEvent(
                AddExpenseFormEvent.SelectedDateChanged(it)
            )
            openDatePicker = false
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text("Add Expense", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Enter the details of your expense", fontSize = 15.sp, color = Color.Gray)

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MoneyInputField(
                label = "Amount",
                modifier = Modifier.weight(1f),
                value = state.amount,
                isError = state.amountError != null,
                onValueChange = { addTransactionViewModel.onExpenseEvent(AddExpenseFormEvent.AmountChanged(it)) }
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Date",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openDatePicker = true }
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.date.toString(),
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        Column {
            Text("Tag")
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                tags.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(
                            selected = (state.tag == tag),
                            onClick = {
                                addTransactionViewModel.onExpenseEvent(
                                    AddExpenseFormEvent.TagChanged(
                                        tag
                                    )
                                )
                            }
                        )
                    ) {
                        RadioButton(
                            selected = (state.tag == tag),
                            onClick = {
                                addTransactionViewModel.onExpenseEvent(
                                    AddExpenseFormEvent.TagChanged(
                                        tag
                                    )
                                )
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color.Blue)
                        )
                        Text(tag)
                    }
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.description,
            onValueChange = { addTransactionViewModel.onExpenseEvent(AddExpenseFormEvent.DescriptionChanged(it)) },
            label = { Text("Expense description") },
            placeholder = { Text("e.g. Taxi") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            isError = state.descriptionError != null
        )

        // Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    addTransactionViewModel.onExpenseEvent(AddExpenseFormEvent.OnSubmit(onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Transaction Added Successfully",
                                actionLabel = "Dismiss"
                            )
                        }
                        onExpenseAdded()
                    }))
                },
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save Transaction", color = Color.White)
            }
            OutlinedButton(
                onClick = {
                    addTransactionViewModel.onExpenseEvent(AddExpenseFormEvent.OnCancel)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }
    }
}
