package com.example.mvvm2026.domain.model.account

/**
 * Returns account information or a failed lookup without exposing data records.
 * Success carries the requested domain accounts, in request order.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
sealed interface AccountResult {
    data class Success(val accounts: List<Account>) : AccountResult
    data object Failed : AccountResult
}
