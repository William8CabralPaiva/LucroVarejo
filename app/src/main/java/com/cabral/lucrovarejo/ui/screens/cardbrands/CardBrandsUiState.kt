package com.cabral.lucrovarejo.ui.screens.cardbrands

import com.cabral.lucrovarejo.domain.payment.CardBrandModel

data class CardBrandsUiState(
    val items: List<CardBrandModel> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
