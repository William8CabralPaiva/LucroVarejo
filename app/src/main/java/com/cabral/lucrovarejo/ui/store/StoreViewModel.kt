package com.cabral.lucrovarejo.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.auth.usecase.GetStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.auth.usecase.UpdateStoreDisplayNameUseCase
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.cabral.lucrovarejo.domain.store.usecase.CreateStoreUseCase
import com.cabral.lucrovarejo.domain.store.usecase.GetStoreSelectionUseCase
import com.cabral.lucrovarejo.domain.store.usecase.SwitchStoreUseCase
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
    private val updateStoreDisplayName: UpdateStoreDisplayNameUseCase,
    private val getStoreSelection: GetStoreSelectionUseCase,
    private val createStoreUseCase: CreateStoreUseCase,
    private val switchStore: SwitchStoreUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreUiState())
    val uiState: StateFlow<StoreUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val name = try {
                getStoreDisplayName()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
            _uiState.update { it.copy(displayName = name) }

            val selection = try {
                getStoreSelection()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
            if (selection != null) {
                _uiState.update {
                    it.copy(stores = selection.stores, activeStoreId = selection.activeStoreId)
                }
            }
        }
    }

    // Aumentar storeEpoch recria o fluxo logado, para as telas observarem a nova loja.
    fun selectStore(storeId: String) {
        val state = _uiState.value
        if (storeId == state.activeStoreId) return
        val store = state.stores.firstOrNull { it.id == storeId } ?: return
        switchStore(storeId)
        activate(store)
    }

    fun createStore(name: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val store = try {
                createStoreUseCase(name.trim())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }
            if (store != null) {
                switchStore(store.id)
                _uiState.update { it.copy(stores = it.stores + store) }
                activate(store)
            }
            onResult(store != null)
        }
    }

    private fun activate(store: StoreSummary) {
        _uiState.update {
            it.copy(
                activeStoreId = store.id,
                displayName = store.displayName.ifBlank { null },
                storeEpoch = it.storeEpoch + 1
            )
        }
    }

    fun clear() {
        loadJob?.cancel()
        _uiState.value = StoreUiState()
    }

    fun showTips() {
        _uiState.update { it.copy(showTips = true) }
    }

    fun dismissTips() {
        _uiState.update { it.copy(showTips = false) }
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
            if (success) {
                _uiState.update { state ->
                    state.copy(
                        displayName = trimmed,
                        stores = state.stores.map {
                            if (it.id == state.activeStoreId) it.copy(displayName = trimmed) else it
                        }
                    )
                }
            }
            onResult(success)
        }
    }

}

data class StoreUiState(
    // Nulo para contas antigas, sem nome da loja cadastrado.
    val displayName: String? = null,
    // Dicas (coachmark) abertas apos o login manual ou pela opcao Dicas em Configuracoes.
    val showTips: Boolean = false,
    val stores: List<StoreSummary> = emptyList(),
    val activeStoreId: String? = null,
    // Muda a cada troca de loja; usado como chave para recriar as telas logadas.
    val storeEpoch: Int = 0,
)