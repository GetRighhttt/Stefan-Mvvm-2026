package com.example.mvvm2026.domain.repository

import com.example.mvvm2026.domain.model.account.AccountResult
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.login.LoginResult

/**
 * Defines authentication and account information lookup using domain models.
 * Implementations validate credentials at the boundary and distinguish invalid input from an account
 * rejection. Account lookup returns typed domain results rather than raw data records. The ViewModel depends on this contract rather than a particular data implementation.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
interface LoginRepository {
    suspend fun getAccountInfo(accountIds: List<String>): AccountResult
    suspend fun login(credentials: Login): LoginResult
}
