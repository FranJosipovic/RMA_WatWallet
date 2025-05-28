package com.example.watwallet.feature.add.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watwallet.feature.add.viewmodel.AddIncomeFormEvent
import com.example.watwallet.feature.add.viewmodel.AddIncomeViewModel
import com.example.watwallet.ui.components.CurrencyDropdown
import com.example.watwallet.ui.components.CustomDatePicker
import com.example.watwallet.ui.components.MoneyInputField
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AddIncomeView(snackbarHostState: SnackbarHostState, addIncomeViewModel: AddIncomeViewModel, onIncomeAdded:()->Unit, onAddJob:()->Unit) {
    val focusManager = LocalFocusManager.current

    val jobs = addIncomeViewModel.jobs

    val state by addIncomeViewModel.state.collectAsState()

    val currencies = listOf("$ USD", "€ EUR", "¥ JPY", "£ GBP", "₹ INR")
    var selectedCurrencyIndex by remember { mutableStateOf(0) }

    var isJobMenuExpanded by remember { mutableStateOf(false) }

    var openDatePicker by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    when{
        openDatePicker -> {
            CustomDatePicker(
                selectedStartDate = state.date,
                onDismissRequest = {openDatePicker = false},
                onSelectDate = { addIncomeViewModel.onEvent(AddIncomeFormEvent.SelectedDateChanged(it)) }
            )
        }
    }

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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Add Income", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Enter the details of your income", fontSize = 15.sp, color = Color.Gray)
        // Job selector row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = state.job?.position ?: "Select Job",
                    onValueChange = {},
                    label = { Text("Job") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown, contentDescription = null,
                            Modifier.clickable { isJobMenuExpanded = true })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isJobMenuExpanded = true }
                )

                DropdownMenu(
                    expanded = isJobMenuExpanded,
                    onDismissRequest = { isJobMenuExpanded = false }
                ) {
                    jobs.forEach {
                        DropdownMenuItem(
                            text = { Text(it.position) },
                            onClick = {
                                addIncomeViewModel.onEvent(AddIncomeFormEvent.JobChanged(it))
                                isJobMenuExpanded = false
                            }
                        )
                    }
                }
            }

            IconButton(
                onClick = {onAddJob()},
                modifier = Modifier
                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(6.dp))
                    .height(56.dp)
                    .width(56.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Add Job") }
        }

        // Base Salary + Tips
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MoneyInputField(
                label = "Base Earned",
                modifier = Modifier.weight(2f),
                value = state.baseEarned,
                isError = state.baseEarnedError != null,
                onValueChange = {
                    addIncomeViewModel.onEvent(AddIncomeFormEvent.BaseEarnedChanged(it))
                }
            )
            MoneyInputField(
                label = "Tips",
                modifier = Modifier.weight(1f),
                value = state.tipsEarned,
                isError = state.tipsEarnedError != null,
                onValueChange = {
                    addIncomeViewModel.onEvent(AddIncomeFormEvent.TipsEarnedChanged(it))
                }
            )
        }

        CurrencyDropdown(
            modifier = Modifier,
            currencies = currencies,
            selectedIndex = selectedCurrencyIndex,
            onCurrencySelected = { selectedCurrencyIndex = it }
        )

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
            Text(
                text = "Total Hours Worked",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = state.totalHoursWorked,
                onValueChange = {
                    addIncomeViewModel.onEvent(AddIncomeFormEvent.TotalHoursWorkedChanged(it))
                },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus() // 👈 this dismisses keyboard & clears focus
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = state.totalHoursWorkedError != null
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    addIncomeViewModel.onEvent(AddIncomeFormEvent.OnSubmit(onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Transaction Added Successfully",
                                actionLabel = "Dismiss"
                            )
                        }
                        onIncomeAdded()
                    }))
                },
                modifier = Modifier.weight(2f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Save Transaction", color = Color.White)
            }
            OutlinedButton(
                onClick = { addIncomeViewModel.onEvent(AddIncomeFormEvent.OnCancel) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }
    }
}


