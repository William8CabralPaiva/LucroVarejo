package com.cabral.lucrovarejo.ui.screens.home

import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary

data class HomeUiState(
    val year: Int,
    val month: Int,
    val summary: MonthlyPurchaseSummary = MonthlyPurchaseSummary(),
    val isLoading: Boolean = true,
    val errorMessage: Int? = null
)
