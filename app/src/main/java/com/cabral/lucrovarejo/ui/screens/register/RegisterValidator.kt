package com.cabral.lucrovarejo.ui.screens.register

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.R

private const val MIN_PASSWORD_LENGTH = 6

internal object RegisterValidator {
    @StringRes fun validateDisplayName(value: String): Int? =
        if (value.isBlank()) R.string.register_display_name_required else null

    private val emailPattern =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    @StringRes fun validateEmail(email: String): Int? = when {
        email.isBlank() -> R.string.register_email_required
        !emailPattern.matches(email) -> R.string.register_email_invalid
        else -> null
    }

    @StringRes fun validatePassword(password: String): Int? = when {
        password.isBlank() -> R.string.register_password_required
        password.length < MIN_PASSWORD_LENGTH -> R.string.register_password_too_short
        else -> null
    }
}
