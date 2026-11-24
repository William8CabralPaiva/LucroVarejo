package com.cabral.lucrovarejo.ui.screens.register

import androidx.annotation.StringRes

data class RegisterUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @param:StringRes val displayNameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val confirmPasswordError: Int? = null,
    @param:StringRes val errorMessage: Int? = null,
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
) {
    val isFormValid: Boolean
        get() = RegisterValidator.validateDisplayName(displayName) == null &&
                RegisterValidator.validateEmail(email) == null &&
                RegisterValidator.validatePassword(password) == null &&
                confirmPassword == password
}
