package com.cabral.lucrovarejo.ui.screens.sales

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel

sealed interface SalesEntry {
    data class Header(val label: String) : SalesEntry
    data class Sale(val purchase: PurchaseModel) : SalesEntry
}