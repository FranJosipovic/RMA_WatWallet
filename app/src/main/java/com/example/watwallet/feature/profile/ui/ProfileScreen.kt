package com.example.watwallet.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.watwallet.R
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.data.repository.JobUser
import com.example.watwallet.data.repository.User
import com.example.watwallet.feature.profile.viewmodel.ProfileViewModel
import com.example.watwallet.ui.components.ConfirmDialog
import com.example.watwallet.utils.DateUtils
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

    ConfirmDialog(
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
            Column(modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                    Text("My Jobs", fontSize = 25.sp)
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

                uiState.value.user!!.userInfo.seasonJobs.forEachIndexed { index, job ->
                    Spacer(modifier = Modifier.height(12.dp))
                    JobCard(
                        job = job.job,
                        onEdit = {
                            navController.navigate(NavigationItem.UpdateJob.createUpdateRoute(job.job.job.uid))
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


@Composable
fun ProfileCard(user:User){
    Box(modifier = Modifier
        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
        .padding(12.dp)
        .fillMaxWidth()
    ){
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .background(color = Color.Blue, shape = CircleShape)
            ){
                Text("${user.userInfo.name.first()}${user.userInfo.surname.first()}", modifier = Modifier.align(Alignment.Center), color = Color.White)
            }
            Column(verticalArrangement = Arrangement.spacedBy(.1.dp)) {
                Text("${user.userInfo.name} ${user.userInfo.surname}")
                Text(user.email)
                Text(user.userInfo.phone)
            }
        }
    }
}

@Composable
fun JobCard(job: JobUser, onEdit:(id:String)->Unit, onDelete:(id:String)->Unit){
    Box(modifier = Modifier
        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(6.dp))
        .fillMaxWidth()
        .padding(12.dp)
    ){
        Row(modifier = Modifier.align(Alignment.TopEnd), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                painter = painterResource(R.drawable.edit_icon),
                tint = Color.Black,
                contentDescription = "Edit icon",
                modifier = Modifier.clickable { onEdit(job.job.uid) }
            )
            Icon(
                painter = painterResource(R.drawable.delete_icon),
                tint = Color.Black,
                contentDescription = "Edit icon",
                modifier = Modifier.clickable {
                    onDelete(job.job.uid)
                }
            )
        }
        Column {
            Text(job.job.employer.name, fontSize = 24.sp)
            Text(job.job.position, color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(job.job.locationInfo, color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Job Description", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(job.job.description, color = Color.Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                Column {
                    Text("Start Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(DateUtils.timestampToLocalDate(job.startDate).toString(),color = Color.Gray, fontSize = 16.sp)
                }
                Column {
                    Text("End Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(DateUtils.timestampToLocalDate(job.endDate).toString(),color = Color.Gray, fontSize = 16.sp)
                }
            }
        }
    }
}
