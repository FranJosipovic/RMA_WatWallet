package com.example.watwallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CustomDatePicker(
    selectedStartDate: LocalDate?,
    onDismissRequest: () -> Unit,
    onSelectDate:(millis:Long)->Unit
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(Color.White)
        ) {
            val initialMillis =
                selectedStartDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()
                    ?: now().toEpochMilliseconds()

            // Initialize the DatePicker state
            val state = rememberDatePickerState(
                initialDisplayMode = DisplayMode.Input,
                initialSelectedDateMillis = initialMillis
            )
            DatePicker(state = state, modifier = Modifier.padding(16.dp))
            Button(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 10.dp),
                onClick = {
                    onSelectDate(state.selectedDateMillis!!)
                    onDismissRequest()
                }
            ) {
                Text("Select Date")
            }
        }
    }
}