package com.example.watwallet.feature.auth.di

import com.example.watwallet.feature.auth.viewmodel.LoginViewModel
import com.example.watwallet.feature.auth.viewmodel.RegisterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

var authViewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
}