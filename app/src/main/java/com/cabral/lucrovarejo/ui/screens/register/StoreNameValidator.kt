package com.cabral.lucrovarejo.ui.screens.register

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.R
import java.util.Locale

object StoreNameValidator {
    fun normalize(value: String): String = value.lowercase(Locale.ROOT)

    @StringRes fun validate(value: String): Int? = when {
        value.isBlank() -> R.string.register_store_required
        value.length !in 3..30 -> R.string.register_store_length
        !value.matches(Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]")) ->
            R.string.register_store_invalid
        else -> null
    }
}

internal object RegisterValidator {
    private val emailPattern =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    @StringRes fun validateEmail(email: String): Int? = when {
        email.isBlank() -> R.string.register_email_required
        !emailPattern.matches(email) -> R.string.register_email_invalid
        else -> null
    }

    @StringRes fun validatePassword(password: String): Int? = when {
        password.isBlank() -> R.string.register_password_required
        password.length < 6 -> R.string.register_password_too_short
        else -> null
    }
}