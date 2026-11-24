package com.cabral.lucrovarejo.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.auth.AuthFailure
import com.cabral.lucrovarejo.domain.auth.AuthFailureReason
import com.cabral.lucrovarejo.domain.auth.StoreAlreadyRegisteredException
import com.cabral.lucrovarejo.domain.auth.usecase.RegisterStoreUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerStore: RegisterStoreUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onGoogleIdToken(idToken: String) {
        val current = _uiState.value
        val displayNameError = RegisterValidator.validateDisplayName(current.displayName)
        if (displayNameError != null) {
            _uiState.update { it.copy(displayNameError = displayNameError) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                signInWithGoogle(idToken, current.displayName)
                _uiState.update { it.copy(isLoading = false, isRegistered = true) }
            } catch (exception: Exception) {
                onGoogleFailure(exception)
            }
        }
    }

    fun onGoogleFailure(exception: Exception? = null) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = exception?.let(::registerErrorMessage) ?: R.string.google_error_generic
            )
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update {
            it.copy(
                displayName = value,
                displayNameError = RegisterValidator.validateDisplayName(value),
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
        val displayNameError = RegisterValidator.validateDisplayName(current.displayName)
        val emailError = RegisterValidator.validateEmail(current.email)
        val passwordError = RegisterValidator.validatePassword(current.password)
        val confirmPasswordError = validateConfirmPassword(
            current.password,
            current.confirmPassword
        )
        val errors = listOfNotNull(
            displayNameError,
            emailError,
            passwordError,
            confirmPasswordError
        )
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    displayNameError = displayNameError,
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
                    email = current.email,
                    password = current.password,
                    displayName = current.displayName
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
