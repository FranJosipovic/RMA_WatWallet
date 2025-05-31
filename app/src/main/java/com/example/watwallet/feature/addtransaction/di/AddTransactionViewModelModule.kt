package com.example.watwallet.feature.addtransaction.di

import com.example.watwallet.feature.addtransaction.viewmodel.AddTransactionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


var addTransactionViewModelModule = module {
    viewModel { AddTransactionViewModel(get(),get(),get(),get()) }
}