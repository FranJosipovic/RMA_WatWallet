package com.example.watwallet.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    show: Boolean,
    selectedStartDate: LocalDate,
    onDismissRequest: () -> Unit,
    onSelectDate: (millis: Long) -> Unit
) {
//    val state = remember(selectedStartDate) {
//        rememberDatePickerState(
//            initialDisplayMode = DisplayMode.Input,
//            initialSelectedDateMillis = selectedStartDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
//        )
//    }

    if (show) {
        key(selectedStartDate) {
            val state = rememberDatePickerState(
                initialDisplayMode = DisplayMode.Input,
                initialSelectedDateMillis = selectedStartDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            )

            DatePickerDialog(
                modifier = Modifier.padding(20.dp),
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { onSelectDate(it) }
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = state, modifier = Modifier.padding(20.dp))
            }
        }
    }
}