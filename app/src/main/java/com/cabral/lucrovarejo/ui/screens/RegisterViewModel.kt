package com.cabral.lucrovarejo.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val storeNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
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

    fun validate(value: String): String? = when {
        value.isBlank() -> "Informe o nome da loja."
        value.length !in 3..30 -> "A loja deve ter de 3 a 30 caracteres."
        !value.matches(Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]")) ->
            "Use letras sem acentos, números, ponto, hífen ou sublinhado, sem espaços."
        else -> null
    }
}

internal object RegisterValidator {
    private val emailPattern =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Informe o e-mail."
        !emailPattern.matches(email) -> "Informe um e-mail válido."
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Informe a senha."
        password.length < 6 -> "A senha deve ter pelo menos 6 caracteres."
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
                        errorMessage = "Esse nome de loja já está em uso."
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
    ): String? =
        when {
            confirmation.isBlank() && required -> "Confirme a senha."
            confirmation.isBlank() -> null
            password != confirmation -> "As senhas não coincidem."
            else -> null
        }

    private fun registerErrorMessage(exception: Exception): String {
        return when ((exception as? AuthFailure)?.reason) {
            AuthFailureReason.PROVIDER_DISABLED ->
                "O cadastro por e-mail e senha está desativado no Firebase Authentication."
            AuthFailureReason.INVALID_ACCOUNT_IDENTIFIER ->
                "O Firebase rejeitou o identificador da conta. Confira a configuração do projeto."
            AuthFailureReason.PERMISSION_DENIED ->
                "O Firebase bloqueou a criação do perfil da loja. Verifique se as regras do Firestore foram publicadas."
            AuthFailureReason.NETWORK ->
                "Sem conexão com o Firebase. Verifique sua internet e tente novamente."
            else ->
                "Não foi possível concluir o cadastro no Firebase. Tente novamente mais tarde."
        }
    }
}
