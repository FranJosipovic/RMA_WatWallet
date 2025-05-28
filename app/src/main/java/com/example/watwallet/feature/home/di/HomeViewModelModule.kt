package com.example.watwallet.feature.home.di

import com.example.watwallet.feature.home.viewmodel.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

var homeViewModelModule = module {
    viewModel { HomeViewModel(get()) }
}