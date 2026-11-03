package com.cabral.lucrovarejo.domain.purchase

data class MonthlyPurchaseSummary(
    val totalSales: Long = 0,
    val totalCost: Long = 0,
    val totalProfit: Long = 0,
    val productsSold: Long = 0,
    // Já incluída em totalCost; exposta à parte para exibição.
    val totalTariff: Long = 0
)
