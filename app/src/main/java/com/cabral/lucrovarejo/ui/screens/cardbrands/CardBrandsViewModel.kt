package com.cabral.lucrovarejo.ui.screens.cardbrands

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.payment.usecase.ArchiveCardBrandUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.CreateCardBrandUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetCardBrandsUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.RenameCardBrandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CardBrandsViewModel @Inject constructor(
    getCardBrands: GetCardBrandsUseCase,
    private val createCardBrand: CreateCardBrandUseCase,
    private val renameCardBrand: RenameCardBrandUseCase,
    private val archiveCardBrand: ArchiveCardBrandUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CardBrandsUiState())
    val uiState: StateFlow<CardBrandsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCardBrands()
                .catch { _uiState.update { state -> state.copy(isLoading = false, hasError = true) } }
                .collect { brands ->
                    _uiState.value = CardBrandsUiState(items = brands, isLoading = false)
                }
        }
    }

    fun onCreate(name: String) {
        viewModelScope.launch {
            runCatching { createCardBrand(name) }
        }
    }

    fun onRename(key: String, name: String) {
        viewModelScope.launch {
            runCatching { renameCardBrand(key, name) }
        }
    }

    fun onArchive(key: String) {
        viewModelScope.launch {
            runCatching { archiveCardBrand(key) }
        }
    }
}
