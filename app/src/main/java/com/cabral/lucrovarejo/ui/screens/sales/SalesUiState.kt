package com.cabral.lucrovarejo.ui.screens.sales

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel

data class SalesUiState(
    val items: List<PurchaseModel> = emptyList(),
    val query: String = "",
    val selectedDateMillis: Long? = null,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val hasError: Boolean = false
)
