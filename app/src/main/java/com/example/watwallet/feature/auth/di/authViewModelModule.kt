package com.example.watwallet.feature.auth.di

import com.example.watwallet.feature.auth.viewmodel.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

var authViewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModelOf(::AuthViewModel)
}