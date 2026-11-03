package com.cabral.lucrovarejo.ui.screens.forgotpassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.auth.usecase.RequestPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val storeName: String = "",
    val email: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isRequestSent: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordReset: RequestPasswordResetUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onStoreNameChanged(value: String) {
        _uiState.update {
            it.copy(
                storeName = value.lowercase(Locale.ROOT),
                errorMessage = null,
                isRequestSent = false
            )
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(email = value.trim(), errorMessage = null, isRequestSent = false)
        }
    }

    fun submit() {
        val current = _uiState.value
        if (current.isLoading) return
        if (current.storeName.isBlank() || current.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Informe o usuário e o e-mail.") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(current.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Informe um e-mail válido.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                requestPasswordReset(current.storeName, current.email)
                _uiState.update { it.copy(isLoading = false, isRequestSent = true) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Não foi possível enviar a solicitação. " +
                            "Verifique sua conexão e tente novamente."
                    )
                }
            }
        }
    }
}
