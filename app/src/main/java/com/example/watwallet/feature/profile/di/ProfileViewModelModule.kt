package com.example.watwallet.feature.profile.di

import com.example.watwallet.feature.profile.viewmodel.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

var profileViewModelModule = module {
    viewModel { ProfileViewModel(get(),get(),get()) }
    viewModelOf(::ProfileViewModel)
}