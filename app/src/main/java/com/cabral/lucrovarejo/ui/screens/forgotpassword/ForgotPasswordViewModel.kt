package com.cabral.lucrovarejo.ui.screens.forgotpassword

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.auth.usecase.RequestPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    @StringRes val errorMessage: Int? = null,
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val requestPasswordReset: RequestPasswordResetUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(
                email = value.trim(),
                errorMessage = null,
                isEmailSent = false
            )
        }
    }

    fun submit() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = R.string.forgot_password_required) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isEmailSent = false) }
        viewModelScope.launch {
            try {
                requestPasswordReset(email)
                _uiState.update { it.copy(isLoading = false, isEmailSent = true) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = R.string.forgot_password_error
                    )
                }
            }
        }
    }
}
