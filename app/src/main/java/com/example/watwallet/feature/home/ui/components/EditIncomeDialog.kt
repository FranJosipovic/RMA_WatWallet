package com.example.watwallet.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import com.example.watwallet.data.repository.JobGetModel
import com.example.watwallet.ui.components.CustomDatePickerDialog
import com.example.watwallet.ui.components.MoneyInputField
import kotlinx.datetime.LocalDate

@Composable
fun EditIncomeDialog(
    jobs: List<JobGetModel>,
    selectedJob: JobGetModel?,
    onJobSelect: (JobGetModel) -> Unit,
    selectedDate: LocalDate,
    onDateSelect: (Long) -> Unit,
    baseEarned: String,
    onBaseEarnedChange: (String) -> Unit,
    tipsEarned: String,
    onTipsEarnedChange: (String) -> Unit,
    totalHoursWorked: String,
    onTotalHoursWorkedChange: (String) -> Unit,
    onSaveChanges: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var openDatePicker by remember { mutableStateOf(false) }

    var isJobMenuExpanded by remember { mutableStateOf(false) }

    CustomDatePickerDialog(
        show = openDatePicker,
        selectedStartDate = selectedDate,
        onDismissRequest = { openDatePicker = false },
        onSelectDate = {
            onDateSelect(it)
            openDatePicker = false
        }
    )
    Dialog(
        onDismissRequest = { onDismissRequest() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedJob?.position ?: "Select Job",
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
                                    onJobSelect(it)
                                    isJobMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Base Salary + Tips
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoneyInputField(
                    label = "Base Earned",
                    modifier = Modifier.weight(2f),
                    value = baseEarned,
                    isError = false,
                    onValueChange = {
                        onBaseEarnedChange(it)
                    }
                )
                MoneyInputField(
                    label = "Tips",
                    modifier = Modifier.weight(1f),
                    value = tipsEarned,
                    isError = false,
                    onValueChange = {
                        onTipsEarnedChange(it)
                    }
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
                            text = selectedDate.toString(),
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
                    value = totalHoursWorked,
                    onValueChange = {
                        onTotalHoursWorkedChange(it)
                    },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    isError = false
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onSaveChanges() },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save Transaction", color = Color.White)
                }
                OutlinedButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}