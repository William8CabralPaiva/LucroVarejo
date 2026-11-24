package com.cabral.lucrovarejo.ui.screens

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.screens.register.RegisterUiState
import com.cabral.lucrovarejo.ui.screens.register.RegisterValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterValidationTest {
    @Test
    fun emailAndPasswordAreValidated() {
        // Given: valid and invalid email/password samples.
        // When: validating their values.
        // Then: only a well-formed email and password of at least six characters pass.
        assertNull(RegisterValidator.validateEmail("owner@example.com"))
        assertEquals(R.string.register_email_invalid, RegisterValidator.validateEmail("invalid"))
        assertNull(RegisterValidator.validatePassword("123456"))
        assertEquals(
            R.string.register_password_too_short,
            RegisterValidator.validatePassword("12345")
        )
    }

    @Test
    fun registrationRequiresMatchingPasswordConfirmation() {
        // Given: otherwise valid registration data with matching and mismatching passwords.
        val matching = RegisterUiState(
            displayName = "Minha Loja",
            email = "owner@example.com",
            password = "123456",
            confirmPassword = "123456"
        )
        val mismatching = matching.copy(confirmPassword = "654321")

        // When: checking if each registration form can be submitted.
        // Then: submission is valid only if confirmation matches the password.
        assertEquals(true, matching.isFormValid)
        assertEquals(false, matching.copy(displayName = "  ").isFormValid)
        assertEquals(false, mismatching.isFormValid)
    }
}
