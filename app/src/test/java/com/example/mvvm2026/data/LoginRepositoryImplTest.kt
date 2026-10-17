package com.example.mvvm2026.data

import com.example.mvvm2026.data.model.account.AccountRecord
import com.example.mvvm2026.data.model.credential.CredentialRecord
import com.example.mvvm2026.data.repository.LoginRepositoryImpl
import com.example.mvvm2026.data.source.AccountDataSource
import com.example.mvvm2026.domain.model.account.Account
import com.example.mvvm2026.domain.model.account.AccountResult
import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.login.LoginResult
import com.example.mvvm2026.domain.model.credential.UsernameError
import com.example.mvvm2026.domain.usecase.ValidateCredentials
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks domain authentication decisions against a fake account data source.
 * Invalid input cannot trigger lookup; missing accounts and mismatched passwords are rejected.
 * Cancellation must reach the caller rather than becoming a failed login.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class LoginRepositoryImplTest {
    private val account = Login("username", "Password1!")

    @Test
    fun `invalid input returns errors without requesting account data`() = runTest {
        val data = FakeAccountDataSource(CredentialRecord(account.username, account.password))
        val result = repository(data).login(Login("ab", "Password1!"))
        assertTrue(result is LoginResult.Invalid)
        assertEquals(UsernameError.Length, (result as LoginResult.Invalid).validation.usernameError)
        assertEquals(emptyList<String>(), data.requestedNames)
    }

    @Test
    fun `matching account succeeds and requests the entered username`() = runTest {
        val data = FakeAccountDataSource(CredentialRecord(account.username, account.password))
        assertEquals(LoginResult.Success, repository(data).login(account))
        assertEquals(listOf("username"), data.requestedNames)
    }

    @Test
    fun `valid but mismatched password is rejected`() = runTest {
        assertEquals(LoginResult.Rejected,
            repository(FakeAccountDataSource(CredentialRecord(account.username, account.password))).login(Login("username", "Different1!")))
    }

    @Test
    fun `missing account is rejected`() = runTest {
        assertEquals(LoginResult.Rejected,
            repository(FakeAccountDataSource(null)).login(account))
    }

    @Test(expected = CancellationException::class)
    fun `account lookup cancellation reaches the caller`() = runTest {
        val data = object : AccountDataSource {
            override suspend fun getAccountInfo(accountIds: List<String>): List<AccountRecord> = emptyList()
            override suspend fun findCredentials(username: String): CredentialRecord? = throw CancellationException()
        }
        repository(data).login(account)
    }

    @Test
    fun `account records map to domain accounts in request order`() = runTest {
        val data = FakeAccountDataSource(null).apply {
            accounts = listOf(AccountRecord("#1", "One"), AccountRecord("#2", "Two"))
        }
        assertEquals(AccountResult.Success(listOf(Account("#2", "Two"), Account("#1", "One"))),
            repository(data).getAccountInfo(listOf("#2", "#1")))
    }

    @Test
    fun `missing requested account returns failure rather than partial information`() = runTest {
        val data = FakeAccountDataSource(null).apply { accounts = listOf(AccountRecord("#1", "One")) }
        assertEquals(AccountResult.Failed, repository(data).getAccountInfo(listOf("#1", "#2")))
    }

    @Test
    fun `empty account request returns empty success`() = runTest {
        assertEquals(AccountResult.Success(emptyList<Account>()),
            repository(FakeAccountDataSource(null)).getAccountInfo(emptyList()))
    }

    private fun repository(data: AccountDataSource) = LoginRepositoryImpl(data, ValidateCredentials())

    private class FakeAccountDataSource(private val account: CredentialRecord?) : AccountDataSource {
        val requestedNames = mutableListOf<String>()
        var accounts = emptyList<AccountRecord>()

        override suspend fun getAccountInfo(accountIds: List<String>): List<AccountRecord> = accounts

        override suspend fun findCredentials(username: String): CredentialRecord? {
            requestedNames += username
            return account
        }
    }
}
