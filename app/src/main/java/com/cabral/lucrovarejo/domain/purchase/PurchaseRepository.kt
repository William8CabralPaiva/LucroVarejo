package com.cabral.lucrovarejo.domain.purchase

import java.util.Date
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel
    fun observeMonthlySummary(start: Date, end: Date): Flow<MonthlyPurchaseSummary>
    fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>>
}