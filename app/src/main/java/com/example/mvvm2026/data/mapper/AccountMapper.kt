package com.example.mvvm2026.data.mapper

import com.example.mvvm2026.data.model.account.AccountRecord
import com.example.mvvm2026.domain.model.account.Account

/**
 * Converts the storage-shaped account record into the domain account value.
 * Mapping at the repository boundary prevents data representation changes from reaching presentation.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
fun AccountRecord.toDomain(): Account = Account(id = id, name = name)
