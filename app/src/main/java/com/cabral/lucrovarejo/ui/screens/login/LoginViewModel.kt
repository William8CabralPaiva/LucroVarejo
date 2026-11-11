package com.cabral.lucrovarejo.ui.screens.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.auth.GoogleSignInResult
import com.cabral.lucrovarejo.domain.auth.usecase.CancelGoogleRegistrationUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.CompleteGoogleRegistrationUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignInUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.SignInWithGoogleUseCase
import com.cabral.lucrovarejo.ui.screens.register.RegisterValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val needsStoreName: Boolean = false,
    val storeName: String = "",
    @param:StringRes val storeNameError: Int? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInStore: SignInUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val completeGoogleRegistration: CompleteGoogleRegistrationUseCase,
    private val cancelGoogleRegistration: CancelGoogleRegistrationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onGoogleIdToken(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                when (signInWithGoogle(idToken)) {
                    GoogleSignInResult.Success ->
                        _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                    GoogleSignInResult.NeedsStoreName ->
                        _uiState.update { it.copy(isLoading = false, needsStoreName = true) }
                }
            } catch (_: Exception) {
                onGoogleFailure()
            }
        }
    }

    fun onGoogleFailure() {
        _uiState.update {
            it.copy(isLoading = false, errorMessage = GOOGLE_ERROR_MESSAGE)
        }
    }

    fun onStoreNameChanged(value: String) {
        _uiState.update {
            it.copy(
                storeName = value,
                storeNameError = RegisterValidator.validateDisplayName(value)
            )
        }
    }

    fun confirmStoreName() {
        val current = _uiState.value
        val error = RegisterValidator.validateDisplayName(current.storeName)
        if (error != null) {
            _uiState.update { it.copy(storeNameError = error) }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                completeGoogleRegistration(current.storeName)
                _uiState.update {
                    it.copy(isLoading = false, needsStoreName = false, isLoggedIn = true)
                }
            } catch (_: Exception) {
                cancelGoogleRegistration()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        needsStoreName = false,
                        errorMessage = GOOGLE_ERROR_MESSAGE
                    )
                }
            }
        }
    }

    fun dismissStoreName() {
        cancelGoogleRegistration()
        _uiState.update {
            it.copy(needsStoreName = false, storeName = "", storeNameError = null)
        }
    }

    fun onEmailChanged(value: String) {
        _uiState.update {
            it.copy(email = value.trim(), errorMessage = null)
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun signIn() {
        val current = _uiState.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Informe o e-mail e a senha.")
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                signInStore(current.email, current.password)
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Não foi possível entrar com esses dados."
                    )
                }
            }
        }
    }

    private companion object {
        const val GOOGLE_ERROR_MESSAGE = "Não foi possível entrar com o Google. Tente novamente."
    }
}
