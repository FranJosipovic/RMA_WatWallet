package com.example.watwallet.feature.add.di

import com.example.watwallet.feature.add.viewmodel.AddIncomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

var addIncomeViewModelModule = module {
    viewModel { AddIncomeViewModel(get(),get(),get()) }
}