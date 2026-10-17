package com.example.mvvm2026.data

import com.example.mvvm2026.data.sourceimpl.AccountDataSourceImpl
import com.example.mvvm2026.data.model.account.AccountRecord
import com.example.mvvm2026.data.model.credential.CredentialRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks simulated account lookup and its delay independently of domain validation.
 * Unknown names return no account, and lookup uses the exact username supplied by the caller.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountDataSourceImplTest {
    private val dataSource = AccountDataSourceImpl()

    @Test
    fun `demo account is available after one second`() = runTest {
        val result = async { dataSource.findCredentials("username") }
        runCurrent()
        advanceTimeBy(999)
        assertFalse(result.isCompleted)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(CredentialRecord("username", "Password1!"), result.await())
    }

    @Test
    fun `unknown username has no account`() = runTest {
        assertNull(dataSource.findCredentials("unknown"))
        assertEquals(1000L, currentTime)
    }

    @Test
    fun `lookup does not normalize the username`() = runTest {
        assertNull(dataSource.findCredentials("Username"))
        assertNull(dataSource.findCredentials(" username "))
    }
    @Test
    fun `account information uses data records and omits unknown IDs`() = runTest {
        assertEquals(listOf(AccountRecord("#2", "Sample account 2"), AccountRecord("#1", "Sample account 1")),
            dataSource.getAccountInfo(listOf("#2", "unknown", "#1")))
        assertEquals(4000L, currentTime)
    }

    @Test
    fun `account information remains pending until four seconds`() = runTest {
        val result = async { dataSource.getAccountInfo(listOf("#1")) }
        runCurrent()
        advanceTimeBy(3999)
        assertFalse(result.isCompleted)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(AccountRecord("#1", "Sample account 1")), result.await())
    }

}
