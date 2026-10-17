package com.example.mvvm2026.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvm2026.data.repository.LoginRepositoryImpl
import com.example.mvvm2026.data.sourceimpl.AccountDataSourceImpl
import com.example.mvvm2026.domain.usecase.ValidateCredentials
import com.example.mvvm2026.presentation.login.MainViewModel

/**
 * Connects the login repository, simulated account data source, and presentation ViewModel.
 * Each ViewModel receives one validator shared with its repository. MainActivity exposes this factory
 * so fragments and tests obtain the same activity-scoped instance without knowing the data layer.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
object LoginViewModelFactory {
    val instance: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val validateCredentials = ValidateCredentials()
            MainViewModel(
                repository = LoginRepositoryImpl(AccountDataSourceImpl(), validateCredentials),
                validateCredentials = validateCredentials
            )
        }
    }
}
