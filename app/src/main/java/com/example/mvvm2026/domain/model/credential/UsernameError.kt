package com.example.mvvm2026.domain.model.credential

/**
 * Identifies the username rule that failed without carrying Android strings.
 * Presentation maps required, length, and character errors to the same text in XML and Compose.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
enum class UsernameError {
    Required,
    Length,
    Characters
}
