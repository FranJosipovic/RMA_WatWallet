package com.example.watwallet.feature.profile.job.di

import android.location.Geocoder
import com.example.watwallet.feature.profile.job.viewmodel.JobViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

var jobViewModelModule = module {
    single { Geocoder(get()) }
    viewModel { JobViewModel(get(),get(),get()) }
}