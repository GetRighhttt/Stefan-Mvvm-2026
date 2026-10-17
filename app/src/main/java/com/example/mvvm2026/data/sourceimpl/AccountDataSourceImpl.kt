package com.example.mvvm2026.data.sourceimpl

import com.example.mvvm2026.data.model.account.AccountRecord
import com.example.mvvm2026.data.model.credential.CredentialRecord
import com.example.mvvm2026.data.source.AccountDataSource
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Simulates account information after four seconds and credential checks after one second.
 * Both delays are cancellable.
 * Six sample accounts and username / Password1! are fixed in-memory data. Missing IDs are omitted;
 * the repository decides whether the returned records satisfy the request. Nothing is persisted.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class AccountDataSourceImpl : AccountDataSource {
    private val credentials = CredentialRecord(username = "username", password = "Password1!")
    private val accounts = (1..6).associate { number ->
        val id = "#$number"
        id to AccountRecord(id = id, name = "Sample account $number")
    }

    override suspend fun getAccountInfo(accountIds: List<String>): List<AccountRecord> {
        delay(4.seconds)
        return accountIds.mapNotNull(accounts::get)
    }

    override suspend fun findCredentials(username: String): CredentialRecord? {
        delay(1.seconds)
        return credentials.takeIf { it.username == username }
    }
}
