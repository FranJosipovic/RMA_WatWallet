package com.example.watwallet.feature.profile.addjob.di

import android.location.Geocoder
import com.example.watwallet.feature.profile.addjob.viewmodel.AddJobViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

var addJobViewModelModule = module {
    single { Geocoder(get()) }
    viewModel { AddJobViewModel(get(),get(),get()) }
}