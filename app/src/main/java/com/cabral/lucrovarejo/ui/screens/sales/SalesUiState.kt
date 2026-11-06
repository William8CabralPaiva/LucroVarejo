package com.cabral.lucrovarejo.ui.screens.sales

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import java.util.Calendar

data class SalesUiState(
    val items: List<PurchaseModel> = emptyList(),
    val query: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val hasError: Boolean = false,
    // null enquanto desconhecido; só false bloqueia a nova venda.
    val hasProducts: Boolean? = null
)
