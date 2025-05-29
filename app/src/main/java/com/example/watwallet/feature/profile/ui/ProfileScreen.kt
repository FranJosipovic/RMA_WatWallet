package com.example.watwallet.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.watwallet.R
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.feature.profile.ui.components.JobCard
import com.example.watwallet.feature.profile.ui.components.ProfileCard
import com.example.watwallet.feature.profile.viewmodel.ProfileViewModel
import com.example.watwallet.ui.components.CustomConfirmDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val profileViewModel: ProfileViewModel = koinViewModel()
    val showConfirmDialog = remember { mutableStateOf(false) }
    val jobToDelete = remember { mutableStateOf<String?>(null) }

    val uiState = profileViewModel.profileUIState.collectAsState()

    val shouldReload = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("reloadJobs", false)
        ?.collectAsState()

    // Trigger the reload if needed
    LaunchedEffect(shouldReload?.value) {
        if (shouldReload?.value == true) {
            profileViewModel.loadUserInfo()
            navController.currentBackStackEntry?.savedStateHandle?.set("reloadJobs", false)
        }
    }

    CustomConfirmDialog(
        showDialog = showConfirmDialog.value,
        description = "Are you sure you want to delete this job?",
        onConfirm = {
            jobToDelete.value?.let { profileViewModel.deleteJob(it) }
            showConfirmDialog.value = false
        },
        onCancel = {
            showConfirmDialog.value = false
        }
    )

    when {
        uiState.value.loading -> {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        uiState.value.user == null && uiState.value.error != null && !uiState.value.loading -> {
            Text(uiState.value.error ?: "")
        }

        uiState.value.user != null && !uiState.value.loading -> {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Profile", fontSize = 35.sp)
                    Button(onClick = {
                        profileViewModel.logout {
                            navController.navigate(NavigationItem.Auth.route) {
                                popUpTo(NavigationItem.Profile.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }) {
                        Text("Logout")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Profile card
                ProfileCard(user = uiState.value.user!!)

                Spacer(modifier = Modifier.height(8.dp))

                // Jobs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "My Jobs", fontSize = 25.sp)
                    Button(
                        onClick = {
                            navController.navigate(NavigationItem.AddJob.route)
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .height(45.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add_icon),
                                tint = Color.White,
                                contentDescription = "Add icon"
                            )
                            Text("Add Job", color = Color.White)
                        }
                    }
                }

                uiState.value.jobs.forEach { job ->
                    Spacer(modifier = Modifier.height(12.dp))
                    JobCard(
                        job = job,
                        onEdit = {
                            navController.navigate(NavigationItem.UpdateJob.createUpdateRoute(job.id))
                        },
                        onDelete = {
                            jobToDelete.value = it
                            showConfirmDialog.value = true
                        }
                    )
                }
            }
        }
    }
}
