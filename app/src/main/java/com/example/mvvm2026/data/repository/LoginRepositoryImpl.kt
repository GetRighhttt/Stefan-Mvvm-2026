package com.example.mvvm2026.data.repository

import com.example.mvvm2026.data.mapper.toDomain
import com.example.mvvm2026.data.source.AccountDataSource
import com.example.mvvm2026.domain.model.account.AccountResult
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.login.LoginResult
import com.example.mvvm2026.domain.repository.LoginRepository
import com.example.mvvm2026.domain.usecase.ValidateCredentials

/**
 * Applies domain validation before credential lookup and maps data records into business results.
 * Credential fields are compared explicitly rather than comparing models from different layers.
 * Account requests succeed only when every requested ID is found; cancellation propagates to the caller.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class LoginRepositoryImpl(
    private val accountDataSource: AccountDataSource,
    private val validateCredentials: ValidateCredentials
) : LoginRepository {
    override suspend fun getAccountInfo(accountIds: List<String>): AccountResult {
        val records = accountDataSource.getAccountInfo(accountIds)
        val accountsById = records.associateBy { it.id }
        if (accountIds.any { it !in accountsById }) return AccountResult.Failed
        return AccountResult.Success(accountIds.map { accountsById.getValue(it).toDomain() })
    }

    override suspend fun login(credentials: Login): LoginResult {
        val validation = validateCredentials(credentials)
        if (!validation.isValid) return LoginResult.Invalid(validation)

        val storedCredentials = accountDataSource.findCredentials(credentials.username)
        return if (storedCredentials?.username == credentials.username && storedCredentials.password == credentials.password) {
            LoginResult.Success
        } else {
            LoginResult.Rejected
        }
    }
}
