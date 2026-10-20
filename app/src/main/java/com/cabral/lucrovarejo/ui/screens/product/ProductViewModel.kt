package com.cabral.lucrovarejo.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductViewModel @Inject constructor(
    getProducts: GetProductsUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(getProducts(), query) { products, searchQuery ->
                products.filter { product ->
                    product.name.contains(searchQuery, ignoreCase = true) ||
                        product.cod.contains(searchQuery, ignoreCase = true)
                } to searchQuery
            }
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, hasError = true)
                    }
                }
                .collect { (products, searchQuery) ->
                    _uiState.value = ProductUiState(
                        items = products,
                        query = searchQuery,
                        isLoading = false
                    )
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }
}
