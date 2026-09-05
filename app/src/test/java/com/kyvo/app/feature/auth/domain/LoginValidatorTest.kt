package com.kyvo.app.feature.auth.domain

import com.kyvo.app.feature.auth.presentation.EmailValidationError
import com.kyvo.app.feature.auth.presentation.PasswordValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginValidatorTest {
    private val validator = LoginValidator()

    @Test
    fun `valid email passes validation`() {
        assertNull(validator.validateEmail("alex@kyvo.app"))
    }

    @Test
    fun `invalid email is rejected`() {
        assertEquals(EmailValidationError.Invalid, validator.validateEmail("alex@kyvo"))
    }

    @Test
    fun `empty email is rejected`() {
        assertEquals(EmailValidationError.Empty, validator.validateEmail("  "))
    }

    @Test
    fun `empty password is rejected`() {
        assertEquals(PasswordValidationError.Empty, validator.validatePassword(""))
    }

    @Test
    fun `short password is rejected`() {
        assertEquals(PasswordValidationError.TooShort, validator.validatePassword("1234567"))
    }
}

