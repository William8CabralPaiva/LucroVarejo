package com.cabral.lucrovarejo.domain.purchase

import java.util.Date

interface PurchaseRepository {
    suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel
    suspend fun getMonthlySummary(start: Date, end: Date): MonthlyPurchaseSummary
    suspend fun getLatestPurchases(): List<PurchaseModel>
}
