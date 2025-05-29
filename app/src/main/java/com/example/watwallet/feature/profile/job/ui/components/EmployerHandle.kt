package com.example.watwallet.feature.profile.job.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.watwallet.data.repository.Employer

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