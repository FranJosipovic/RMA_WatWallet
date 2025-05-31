package com.example.watwallet.feature.profile.job.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.watwallet.feature.profile.job.viewmodel.JobViewModel
import com.example.watwallet.ui.components.CustomDatePickerDialog
import com.example.watwallet.ui.components.LabeledInputField
import com.example.watwallet.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobForm(
    jobId: String?,
    jobViewModel: JobViewModel,
    onJobAction: () -> Unit
) {

    val jobForm by jobViewModel.jobForm.collectAsState()

    val employerSearchField by jobViewModel.employerSearchField.collectAsState()
    val employersSearch by jobViewModel.employersSearchResultsState.collectAsState()

    val locationSearchField by jobViewModel.locationSearchField.collectAsState()
    val locationSearchList by jobViewModel.locationSearchResults.collectAsState()

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    LaunchedEffect(jobId) {
        if (!jobId.isNullOrEmpty()) {
            jobViewModel.getJobInfo(jobId = jobId) { startDate, endDate ->
                startDatePickerState.selectedDateMillis =
                    DateUtils.localDateToMillis(startDate)
                endDatePickerState.selectedDateMillis = DateUtils.localDateToMillis(endDate)
            }
        }
    }

    var openStartDateDatePicker by remember { mutableStateOf(false) }
    var openEndDateDatePicker by remember { mutableStateOf(false) }

    CustomDatePickerDialog(
        show = openStartDateDatePicker,
        selectedStartDate = jobForm.startDate,
        onDismissRequest = { openStartDateDatePicker = false },
        onSelectDate = {
            jobViewModel.onSelectStartDate(it)
            openStartDateDatePicker = false
        }
    )

    CustomDatePickerDialog(
        show = openEndDateDatePicker,
        selectedStartDate = jobForm.endDate,
        onDismissRequest = { openEndDateDatePicker = false },
        onSelectDate = {
            jobViewModel.onSelectEndDate(it)
            openEndDateDatePicker = false
        }
    )

    when {
        jobForm.loading -> {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        else -> {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        EmployerHandle(
                            selectedEmployer = jobForm.employer,
                            employerSearchField = employerSearchField,
                            onQueryChange = { jobViewModel.onEmployerFieldChange(it) },
                            onSelectEmployer = { jobViewModel.selectEmployer(it) },
                            onCreateEmployer = { employerName ->
                                jobViewModel.createEmployer(
                                    employerName = employerName,
                                    onSuccess = { jobViewModel.onEmployerFieldChange(it.name) }
                                )
                            },
                            onClearSearchBar = {
                                jobViewModel.onEmployerFieldChange("")
                                jobViewModel.unselectEmployer()
                            },
                            employersSearch = employersSearch
                        )

                        Spacer(Modifier.height(20.dp))

                        LabeledInputField(
                            label = "Job Position",
                            placeholder = "e.g. Food Runner",
                            value = jobForm.position,
                            onValueChange = { jobViewModel.onJobPositionUpdate(it) }
                        )

                        Spacer(Modifier.height(20.dp))

                        LocationHandle(
                            locationSearchValue = locationSearchField,
                            onLocationSearchChange = {
                                jobViewModel.onLocationSearchChange(it)
                            },
                            selectedLocation = jobForm.location,
                            locationsSearch = locationSearchList,
                            onLocationSelect = {
                                jobViewModel.onLocationSelect(it)
                            }
                        )

                        Spacer(Modifier.height(20.dp))

                        val focusManager = LocalFocusManager.current
                        Text("Job Description", fontSize = 20.sp)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = jobForm.description,
                            onValueChange = {
                                jobViewModel.onJobDescriptionChange(it)
                            },
                            singleLine = false,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            placeholder = { Text("e.g. Delivering food to guests tables...") }
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Start Date", fontSize = 20.sp)
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = {
                                    openStartDateDatePicker = true
                                }) {
                                    Text(jobForm.startDate.toString())
                                }
                            }

                            Column {
                                Text("End Date", fontSize = 20.sp)
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = {
                                    openEndDateDatePicker = true
                                }) {
                                    Text(jobForm.endDate.toString())
                                }
                            }
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        onClick = {
                            if (jobId != null) {
                                jobViewModel.onUpdateJob(
                                    jobId = jobId,
                                    onSuccess = {
                                        onJobAction()
                                    }
                                )
                            } else {
                                jobViewModel.onAddJob {
                                    onJobAction()
                                }
                            }

                        },

                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            if (jobId != null) {
                                "Save Job Changes"
                            } else {
                                "Add New Job"
                            }
                        )
                    }
                }
            }
        }
    }
}
