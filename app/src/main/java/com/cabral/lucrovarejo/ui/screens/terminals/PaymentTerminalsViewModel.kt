package com.cabral.lucrovarejo.ui.screens.terminals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.payment.usecase.ArchiveTerminalUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PaymentTerminalsViewModel @Inject constructor(
    getTerminals: GetTerminalsUseCase,
    private val archiveTerminal: ArchiveTerminalUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentTerminalsUiState())
    val uiState: StateFlow<PaymentTerminalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTerminals()
                .catch { _uiState.update { state -> state.copy(isLoading = false, hasError = true) } }
                .collect { terminals ->
                    _uiState.value = PaymentTerminalsUiState(items = terminals, isLoading = false)
                }
        }
    }

    fun onArchive(key: String) {
        viewModelScope.launch {
            runCatching { archiveTerminal(key) }
        }
    }
}
