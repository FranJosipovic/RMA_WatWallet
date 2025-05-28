package com.example.watwallet.feature.add.di

import com.example.watwallet.feature.add.viewmodel.AddExpenseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


var addExpenseViewModelModule = module {
    viewModel { AddExpenseViewModel(get(),get(),get()) }
}