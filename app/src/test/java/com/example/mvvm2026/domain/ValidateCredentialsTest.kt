package com.example.mvvm2026.domain

import com.example.mvvm2026.domain.model.login.Login
import com.example.mvvm2026.domain.model.credential.PasswordError
import com.example.mvvm2026.domain.model.credential.UsernameError
import com.example.mvvm2026.domain.usecase.ValidateCredentials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks length boundaries and each required character category without Android.
 * Combined validation also verifies that both field errors are returned in the same result.
 *
 * @author Stefan Bayne
 * @since 2026-10-17
 */
class ValidateCredentialsTest {
    private val validate = ValidateCredentials()

    @Test
    fun `username accepts both cases digits and underscores`() {
        assertNull(validate.usernameError("User_name123"))
    }

    @Test
    fun `username accepts both length limits`() {
        assertNull(validate.usernameError("a".repeat(ValidateCredentials.USERNAME_MIN_LENGTH)))
        assertNull(validate.usernameError("a".repeat(ValidateCredentials.USERNAME_MAX_LENGTH)))
    }

    @Test
    fun `username rejects lengths outside the limits`() {
        assertEquals(UsernameError.Length, validate.usernameError("ab"))
        assertEquals(UsernameError.Length, validate.usernameError("a".repeat(21)))
    }

    @Test
    fun `username rejects blank spaces punctuation and non ASCII letters`() {
        assertEquals(UsernameError.Required, validate.usernameError(" "))
        listOf("user name", "user-name", "username!", "usérname").forEach {
            assertEquals(it, UsernameError.Characters, validate.usernameError(it))
        }
    }

    @Test
    fun `password accepts all categories at both length limits`() {
        assertNull(validate.passwordError("Aa1!aaaa"))
        assertNull(validate.passwordError("Aa1!" + "a".repeat(60)))
        assertNull(validate.passwordError("Password1!"))
    }

    @Test
    fun `password rejects lengths outside the limits`() {
        assertEquals(PasswordError.Length, validate.passwordError("Aa1!aaa"))
        assertEquals(PasswordError.Length, validate.passwordError("Aa1!" + "a".repeat(61)))
    }

    @Test
    fun `password requires uppercase lowercase digit and symbol separately`() {
        listOf("password1!", "PASSWORD1!", "Password!!", "Password12").forEach {
            assertEquals(it, PasswordError.Complexity, validate.passwordError(it))
        }
    }

    @Test
    fun `password rejects blank input and whitespace within an otherwise valid password`() {
        assertEquals(PasswordError.Required, validate.passwordError(""))
        assertEquals(PasswordError.Required, validate.passwordError("   "))
        assertEquals(PasswordError.Whitespace, validate.passwordError("Pass word1!"))
        assertEquals(PasswordError.Whitespace, validate.passwordError("Password1!\n"))
    }

    @Test
    fun `validation returns both errors and accepts the demo credentials`() {
        val invalid = validate(Login("ab", "short"))
        assertFalse(invalid.isValid)
        assertEquals(UsernameError.Length, invalid.usernameError)
        assertEquals(PasswordError.Length, invalid.passwordError)
        assertTrue(validate(Login("username", "Password1!")).isValid)
    }
}
