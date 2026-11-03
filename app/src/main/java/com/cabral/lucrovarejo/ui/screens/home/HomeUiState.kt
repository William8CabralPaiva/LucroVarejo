package com.cabral.lucrovarejo.ui.screens.home

import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel

data class HomeUiState(
    val year: Int,
    val month: Int,
    val summary: MonthlyPurchaseSummary = MonthlyPurchaseSummary(),
    val isLoading: Boolean = true,
    val errorMessage: Int? = null,
    val latestPurchases: List<PurchaseModel> = emptyList(),
    val isLatestPurchasesLoading: Boolean = true,
    val hasLatestPurchasesError: Boolean = false
)