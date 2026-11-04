package com.cabral.lucrovarejo.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.auth.usecase.GetStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.UpdateStoreDisplayNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
    private val getStoreDisplayName: GetStoreDisplayNameUseCase,
    private val updateStoreDisplayName: UpdateStoreDisplayNameUseCase
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

    // O nome fica no estado compartilhado, entao a Home e as demais telas atualizam sozinhas.
    fun updateDisplayName(name: String, onResult: (Boolean) -> Unit) {
        val trimmed = name.trim()
        viewModelScope.launch {
            val success = try {
                updateStoreDisplayName(trimmed)
                true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                false
            }
            if (success) _uiState.update { it.copy(displayName = trimmed) }
            onResult(success)
        }
    }

}

data class StoreUiState(
    // Nulo para contas antigas, sem nome da loja cadastrado.
    val displayName: String? = null,
)