package com.example.watwallet.data.di

import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.AuthRepositoryImpl
import com.example.watwallet.data.repository.EmployerRepository
import com.example.watwallet.data.repository.EmployerRepositoryImpl
import com.example.watwallet.data.repository.JobRepository
import com.example.watwallet.data.repository.JobRepositoryImpl
import com.example.watwallet.data.repository.TransactionRepositoryImpl
import com.example.watwallet.data.repository.TransactionsRepository
import com.example.watwallet.data.repository.UserRepository
import com.example.watwallet.data.repository.UserRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    single<UserRepository> { UserRepositoryImpl() }
    single<EmployerRepository> { EmployerRepositoryImpl() }
    single<JobRepository> { JobRepositoryImpl(get()) }
    single<TransactionsRepository> { TransactionRepositoryImpl() }
}