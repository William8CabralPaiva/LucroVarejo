package com.cabral.lucrovarejo.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.auth.usecase.GetStoreDisplayNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// Escopo da Activity: compartilhado por todo o app logado.
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getStoreDisplayName: GetStoreDisplayNameUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val name = try {
                getStoreDisplayName()
            } catch (_: Exception) {
                null
            }
            _uiState.update { it.copy(displayName = name) }
        }
    }

    fun clear() {
        loadJob?.cancel()
        _uiState.value = StoreUiState()
    }

}

data class StoreUiState(
    // Nulo para contas antigas, sem nome da loja cadastrado.
    val displayName: String? = null,
)