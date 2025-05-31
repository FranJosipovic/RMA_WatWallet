package com.example.watwallet.feature.profile.job.ui

import androidx.compose.runtime.Composable
import com.example.watwallet.feature.profile.job.ui.components.JobForm
import com.example.watwallet.feature.profile.job.viewmodel.JobViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddJobRoute(onJobAdded: () -> Unit) {
    val jobViewModel: JobViewModel = koinViewModel()

    JobForm(jobId = null, jobViewModel = jobViewModel, onJobAction = { onJobAdded() })
}



