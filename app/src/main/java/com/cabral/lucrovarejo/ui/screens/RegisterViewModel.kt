package com.cabral.lucrovarejo.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.auth.AuthFailure
import com.cabral.lucrovarejo.domain.auth.AuthFailureReason
import com.cabral.lucrovarejo.domain.auth.StoreAlreadyRegisteredException
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class RegisterUiState(
    val storeName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @param:StringRes val storeNameError: Int? = null,
    @param:StringRes val emailError: Int? = null,
    @param:StringRes val passwordError: Int? = null,
    @param:StringRes val confirmPasswordError: Int? = null,
    @param:StringRes val errorMessage: Int? = null,
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false
) {
    val isFormValid: Boolean
        get() = StoreNameValidator.validate(storeName) == null &&
            RegisterValidator.validateEmail(email) == null &&
            RegisterValidator.validatePassword(password) == null &&
            confirmPassword == password
}

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

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerStore: RegisterStoreUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onStoreNameChanged(value: String) {
        val normalized = StoreNameValidator.normalize(value)
        _uiState.update {
            it.copy(
                storeName = normalized,
                storeNameError = StoreNameValidator.validate(normalized),
                errorMessage = null,
                isRegistered = false
            )
        }
    }

    fun onEmailChanged(value: String) {
        val email = value.trim()
        _uiState.update {
            it.copy(
                email = email,
                emailError = RegisterValidator.validateEmail(email),
                errorMessage = null,
                isRegistered = false
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = RegisterValidator.validatePassword(value),
                confirmPasswordError = validateConfirmPassword(
                    value,
                    it.confirmPassword,
                    required = false
                ),
                errorMessage = null,
                isRegistered = false
            )
        }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                confirmPassword = value,
                confirmPasswordError = validateConfirmPassword(
                    it.password,
                    value,
                    required = false
                ),
                errorMessage = null,
                isRegistered = false
            )
        }
    }

    fun register() {
        val current = _uiState.value
        val storeError = StoreNameValidator.validate(current.storeName)
        val emailError = RegisterValidator.validateEmail(current.email)
        val passwordError = RegisterValidator.validatePassword(current.password)
        val confirmPasswordError = validateConfirmPassword(
            current.password,
            current.confirmPassword
        )
        if (storeError != null || emailError != null || passwordError != null ||
            confirmPasswordError != null
        ) {
            _uiState.update {
                it.copy(
                    storeNameError = storeError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                registerStore(
                    storeName = current.storeName.lowercase(Locale.ROOT),
                    email = current.email,
                    password = current.password
                )
                _uiState.update { it.copy(isLoading = false, isRegistered = true) }
            } catch (_: StoreAlreadyRegisteredException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = R.string.register_store_taken
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = registerErrorMessage(exception)
                    )
                }
            }
        }
    }

    private fun validateConfirmPassword(
        password: String,
        confirmation: String,
        required: Boolean = true
    ): Int? =
        when {
            confirmation.isBlank() && required -> R.string.register_confirm_password_required
            confirmation.isBlank() -> null
            password != confirmation -> R.string.register_password_mismatch
            else -> null
        }

    @StringRes private fun registerErrorMessage(exception: Exception): Int {
        return when ((exception as? AuthFailure)?.reason) {
            AuthFailureReason.PROVIDER_DISABLED ->
                R.string.register_error_provider_disabled
            AuthFailureReason.INVALID_ACCOUNT_IDENTIFIER ->
                R.string.register_error_invalid_identifier
            AuthFailureReason.PERMISSION_DENIED ->
                R.string.register_error_permission_denied
            AuthFailureReason.NETWORK ->
                R.string.register_error_network
            else ->
                R.string.register_error_generic
        }
    }
}
