package com.example.mvvm2026.domain.model.credential

/**
 * Identifies missing input, invalid length, whitespace, or missing character categories.
 * The validator chooses one error per field; presentation turns that value into corrective guidance.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
enum class PasswordError {
    Required,
    Length,
    Whitespace,
    Complexity
}
