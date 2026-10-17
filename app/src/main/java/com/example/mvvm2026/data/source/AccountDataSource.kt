package com.example.mvvm2026.data.source

import com.example.mvvm2026.data.model.account.AccountRecord
import com.example.mvvm2026.data.model.credential.CredentialRecord

/**
 * Supplies simulated account and credential records for repository operations.
 * Only data models cross this internal boundary; validation and domain-result mapping belong to the repository.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
interface AccountDataSource {
    suspend fun getAccountInfo(accountIds: List<String>): List<AccountRecord>
    suspend fun findCredentials(username: String): CredentialRecord?
}
