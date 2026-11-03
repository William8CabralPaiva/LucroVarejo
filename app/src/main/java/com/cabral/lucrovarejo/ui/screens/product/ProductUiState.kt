package com.cabral.lucrovarejo.ui.screens.product

import com.cabral.lucrovarejo.domain.product.ProductModel

data class ProductUiState(
    val items: List<ProductModel> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
