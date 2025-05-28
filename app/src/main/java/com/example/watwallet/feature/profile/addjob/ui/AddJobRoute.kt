package com.example.watwallet.feature.profile.addjob.ui

import android.location.Address
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.watwallet.data.repository.Employer
import com.example.watwallet.feature.profile.addjob.viewmodel.AddJobViewModel
import com.example.watwallet.ui.components.LabeledInputField
import com.example.watwallet.utils.DateUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddJobRoute(onJobAdded: () -> Unit) {
    val addJobViewModel: AddJobViewModel = koinViewModel()

    JobForm(null, addJobViewModel) {
        onJobAdded()
    }
}

@Composable
fun UpdateJobRoute(jobId: String, onJobUpdated: () -> Unit) {
    val addJobViewModel: AddJobViewModel = koinViewModel()

    JobForm(jobId, addJobViewModel) {
        onJobUpdated()
    }
}


@ExperimentalMaterial3Api
@Composable
fun EmployerHandle(
    selectedEmployer: Employer?,
    employerSearchField: String,
    employersSearch: List<Employer>,
    onQueryChange: (String) -> Unit,
    onSelectEmployer: (employer: Employer) -> Unit,
    onCreateEmployer: (name: String) -> Unit,
    onClearSearchBar: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    if (selectedEmployer != null) {
        Text("Employer")
        Spacer(Modifier.height(10.dp))
    }

    Row {
        if (selectedEmployer != null) {
            OutlinedCard(
                modifier = Modifier
                    .weight(5f)
                    .height(55.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(selectedEmployer.name, modifier = Modifier.padding(horizontal = 10.dp))
                }
            }
            Spacer(Modifier.width(10.dp))
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .height(55.dp),
            onClick = { showBottomSheet = true },
            shape = RoundedCornerShape(4.dp)
        ) {
            if (selectedEmployer != null) {
                Icon(imageVector = Icons.Default.Add, "add employer")
            } else {
                Text("Select Employer")
            }
        }
    }

    var active by rememberSaveable { mutableStateOf(false) }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            SearchBar(
                query = employerSearchField,
                onQueryChange = {
                    onQueryChange(it)
                },
                onSearch = {},
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("e.g. The Boardwalk") },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(15.dp),
                trailingIcon = {
                    if (!active)
                        Icon(imageVector = Icons.Default.Search, "search icon")
                    else
                        Icon(
                            imageVector = Icons.Default.Clear,
                            "clear icon",
                            modifier = Modifier.clickable {
                                active = false
                                onClearSearchBar()
                            })
                }
            ) {
                employersSearch.forEach {
                    ListItem(headlineContent = { Text(it.name) }, modifier = Modifier
                        .clickable {
                            onQueryChange(it.name)
                            active = false
                            onSelectEmployer(it)
                        }
                        .fillMaxWidth())
                }
            }

            val openDialog = remember { mutableStateOf(false) }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                onClick = {
                    if (selectedEmployer != null) {
                        showBottomSheet = false
                    } else {
                        openDialog.value = true
                    }
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                if (selectedEmployer != null)
                    Text("Confirm")
                else
                    Text("Create new employer")
            }

            when {
                openDialog.value ->
                    CreateEmployerDialog(
                        onDismissRequest = { openDialog.value = false },
                        onConfirm = {
                            onCreateEmployer(it)
                        })
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun LocationHandle(
    locationSearchValue: String,
    onLocationSearchChange: (value: String) -> Unit,
    selectedLocation: Address?,
    locationsSearch: List<Address>,
    onLocationSelect: (Address) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Properly handle map state to reflect location changes
    var startLocation by remember {
        mutableStateOf(selectedLocation?.let { LatLng(it.latitude, it.longitude) })
    }

    val markerState = remember {
        mutableStateOf(
            startLocation?.let { MarkerState(position = it) }
        )
    }

    val cameraPositionState = remember {
        mutableStateOf(
            startLocation?.let {
                CameraPositionState(position = CameraPosition.fromLatLngZoom(it, 10f))
            }
        )
    }

    Row {
        Button(
            modifier = Modifier
                .weight(1f)
                .height(55.dp),
            onClick = { showBottomSheet = true },
            shape = RoundedCornerShape(4.dp)
        ) {
            if (selectedLocation != null) {
                Text("${selectedLocation.featureName}, ${selectedLocation.countryName}")
            } else {
                Text("Select Location")
            }
        }
    }


    var active by rememberSaveable { mutableStateOf(false) }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = locationSearchValue,
                        onQueryChange = {
                            onLocationSearchChange(it)
                        },
                        onSearch = {},
                        expanded = active,
                        onExpandedChange = { active = it },
                        enabled = true,
                        placeholder = { Text("e.g. The Boardwalk") },
                        leadingIcon = null,
                        trailingIcon = {
                            if (!active)
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "search icon"
                                )
                            else
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "clear icon",
                                    modifier = Modifier.clickable {
                                        active = false
                                        onLocationSearchChange("")
                                    }
                                )
                        },
                        interactionSource = null,
                    )
                },
                expanded = active,
                onExpandedChange = { active = it },
                modifier = Modifier.padding(15.dp),
                shape = RoundedCornerShape(4.dp),
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation,
                windowInsets = SearchBarDefaults.windowInsets
            ) {
                locationsSearch.forEach { address ->
                    ListItem(
                        headlineContent = { Text("${address.featureName}, ${address.countryName}") },
                        modifier = Modifier
                            .clickable {
                                // Update location and marker state
                                val newLatLng = LatLng(address.latitude, address.longitude)
                                startLocation = newLatLng
                                markerState.value = MarkerState(position = newLatLng)
                                cameraPositionState.value = CameraPositionState(
                                    position = CameraPosition.fromLatLngZoom(newLatLng, 5f)
                                )

                                // Inform the view model about the selected location
                                onLocationSelect(address)
                                active = false
                            }
                            .fillMaxWidth()
                    )
                }
            }

            // Show the map only if the start location is available
            if (startLocation != null && markerState.value != null && cameraPositionState.value != null && !active) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(10.dp),
                    cameraPositionState = cameraPositionState.value!!
                ) {
                    Marker(
                        state = markerState.value!!,
                        title = "Selected Location",
                        snippet = "Start Marker"
                    )
                }
            }

        }
    }
}


@Composable
fun CreateEmployerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (employerName: String) -> Unit,
) {
    var employerName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(color = Color.White)
                .padding(20.dp)
        ) {

            Text(
                "Add New Employer",
                fontSize = 26.sp,
                modifier = Modifier.align(Alignment.TopStart),
                color = Color.Gray
            )
            Column(
                modifier = Modifier.padding(top = 30.dp)
            ) {
                Spacer(Modifier.height(10.dp))
                LabeledInputField(
                    label = "Employer Name",
                    placeholder = "e.g. The Boardwalk",
                    employerName,
                    onValueChange = { employerName = it }
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.End) {
                    Spacer(Modifier.weight(1f))
                    TextButton({ onDismissRequest() }) {
                        Text("Close")
                    }
                    Spacer(Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onConfirm(employerName)
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Confirm")
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Close",
                modifier = Modifier
                    .clickable { onDismissRequest() }
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobForm(
    jobId: String?,
    addJobViewModel: AddJobViewModel,
    onJobAction: () -> Unit
) {

    val jobForm by addJobViewModel.jobForm.collectAsState()

    val employerSearchField by addJobViewModel.employerSearchField.collectAsState()
    val employersSearch by addJobViewModel.employersSearchResultsState.collectAsState()

    val locationSearchField by addJobViewModel.locationSearchField.collectAsState()
    val locationSearchList by addJobViewModel.locationSearchResults.collectAsState()

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    LaunchedEffect(jobId) {
        if (!jobId.isNullOrEmpty()) {
            addJobViewModel.getJobInfo(jobId = jobId) { startDate, endDate ->
                startDatePickerState.selectedDateMillis =
                    DateUtils.localDateToMillis(startDate)
                endDatePickerState.selectedDateMillis = DateUtils.localDateToMillis(endDate)
            }
        }
    }

    var openStartDateDatePicker by remember { mutableStateOf(false) }
    var openEndDateDatePicker by remember { mutableStateOf(false) }

    when {
        openStartDateDatePicker -> {
            DatePickerDialog(
                modifier = Modifier.padding(20.dp),
                onDismissRequest = { openStartDateDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        addJobViewModel.onSelectStartDate(startDatePickerState.selectedDateMillis!!)
                        openStartDateDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openStartDateDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = startDatePickerState, modifier = Modifier.padding(20.dp))
            }
        }

        openEndDateDatePicker -> {
            DatePickerDialog(
                modifier = Modifier.padding(20.dp),
                onDismissRequest = { openEndDateDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        addJobViewModel.onSelectEndDate(endDatePickerState.selectedDateMillis!!)
                        openEndDateDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openEndDateDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = endDatePickerState, modifier = Modifier.padding(20.dp))
            }
        }
    }

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
                            onQueryChange = { addJobViewModel.onEmployerFieldChange(it) },
                            onSelectEmployer = { addJobViewModel.selectEmployer(it) },
                            onCreateEmployer = { employerName ->
                                addJobViewModel.createEmployer(
                                    employerName = employerName,
                                    onSuccess = { addJobViewModel.onEmployerFieldChange(it.name) }
                                )
                            },
                            onClearSearchBar = {
                                addJobViewModel.onEmployerFieldChange("")
                                addJobViewModel.unselectEmployer()
                            },
                            employersSearch = employersSearch
                        )

                        Spacer(Modifier.height(20.dp))

                        LabeledInputField(
                            label = "Job Position",
                            placeholder = "e.g. Food Runner",
                            value = jobForm.position,
                            onValueChange = { addJobViewModel.onJobPositionUpdate(it) }
                        )

                        Spacer(Modifier.height(20.dp))

                        LocationHandle(
                            locationSearchValue = locationSearchField,
                            onLocationSearchChange = {
                                addJobViewModel.onLocationSearchChange(it)
                            },
                            selectedLocation = jobForm.location,
                            locationsSearch = locationSearchList,
                            onLocationSelect = {
                                addJobViewModel.onLocationSelect(it)
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
                                addJobViewModel.onJobDescriptionChange(it)
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
                                addJobViewModel.onUpdateJob(
                                    jobId = jobId,
                                    onSuccess = {
                                        onJobAction()
                                    }
                                )
                            } else {
                                addJobViewModel.onAddJob {
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