package com.example.watwallet.app

import android.app.Application
import com.example.watwallet.core.di.networkModule
import com.example.watwallet.data.di.repositoryModule
import com.example.watwallet.feature.add.di.addExpenseViewModelModule
import com.example.watwallet.feature.add.di.addIncomeViewModelModule
import com.example.watwallet.feature.auth.di.authViewModelModule
import com.example.watwallet.feature.home.di.homeViewModelModule
import com.example.watwallet.feature.profile.addjob.di.addJobViewModelModule
import com.example.watwallet.feature.profile.di.profileViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WatWalletApp : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidContext(this@WatWalletApp)
            modules(
                networkModule,
                repositoryModule,
                authViewModelModule,
                profileViewModelModule,
                addIncomeViewModelModule,
                addExpenseViewModelModule,
                addJobViewModelModule,
                homeViewModelModule
            )
        }
    }
}