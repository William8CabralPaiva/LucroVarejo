package com.cabral.lucrovarejo.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterValidationTest {
    @Test
    fun storeNameIsLowercasedWithoutRemovingInvalidCharacters() {
        assertEquals("lojacentral", StoreNameValidator.normalize("LojaCentral"))
        assertEquals("loja são", StoreNameValidator.normalize("Loja São"))
    }

    @Test
    fun storeNameRejectsSpacesAndAccents() {
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
        val matching = RegisterUiState(
            storeName = "loja123",
            email = "owner@example.com",
            password = "123456",
            confirmPassword = "123456"
        )
        val mismatching = matching.copy(confirmPassword = "654321")

        assertEquals(true, matching.isFormValid)
        assertEquals(false, mismatching.isFormValid)
    }
}
