package com.cabral.lucrovarejo.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterValidationTest {
    @Test
    fun storeNameIsLowercasedWithoutRemovingInvalidCharacters() {
        // Given: store names with mixed casing and unsupported characters.
        // When: normalizing the values.
        // Then: casing is normalized but invalid characters remain available for validation.
        assertEquals("lojacentral", StoreNameValidator.normalize("LojaCentral"))
        assertEquals("loja são", StoreNameValidator.normalize("Loja São"))
    }

    @Test
    fun storeNameRejectsSpacesAndAccents() {
        // Given: store names containing spaces or accent marks.
        // When: validating the names.
        // Then: both values are rejected with the store-name rule.
        assertEquals(
            "Use letras sem acentos, números, ponto, hífen ou sublinhado, sem espaços.",
            StoreNameValidator.validate("loja central")
        )
        assertEquals(
            "Use letras sem acentos, números, ponto, hífen ou sublinhado, sem espaços.",
            StoreNameValidator.validate("lojasão")
        )
    }

    @Test
    fun storeNameRequiresThreeToThirtyAsciiCharacters() {
        // Given: names below, above, and within the supported length.
        // When: validating each name.
        // Then: only the 3-30 character ASCII identifier is accepted.
        assertEquals(
            "A loja deve ter de 3 a 30 caracteres.",
            StoreNameValidator.validate("ab")
        )
        assertEquals(
            "A loja deve ter de 3 a 30 caracteres.",
            StoreNameValidator.validate("loja".repeat(8))
        )
        assertNull(StoreNameValidator.validate("loja123"))
        assertNull(StoreNameValidator.validate("loja_123"))
        assertNull(StoreNameValidator.validate("loja-123"))
        assertNull(StoreNameValidator.validate("loja.123"))
    }

    @Test
    fun emailAndPasswordAreValidated() {
        // Given: valid and invalid email/password samples.
        // When: validating their values.
        // Then: only a well-formed email and password of at least six characters pass.
        assertNull(RegisterValidator.validateEmail("owner@example.com"))
        assertEquals("Informe um e-mail válido.", RegisterValidator.validateEmail("invalid"))
        assertNull(RegisterValidator.validatePassword("123456"))
        assertEquals(
            "A senha deve ter pelo menos 6 caracteres.",
            RegisterValidator.validatePassword("12345")
        )
    }

    @Test
    fun registrationRequiresMatchingPasswordConfirmation() {
        // Given: otherwise valid registration data with matching and mismatching passwords.
        val matching = RegisterUiState(
            storeName = "loja123",
            email = "owner@example.com",
            password = "123456",
            confirmPassword = "123456"
        )
        val mismatching = matching.copy(confirmPassword = "654321")

        // When: checking if each registration form can be submitted.
        // Then: submission is valid only if confirmation matches the password.
        assertEquals(true, matching.isFormValid)
        assertEquals(false, mismatching.isFormValid)
    }
}
