package com.cabral.lucrovarejo.domain.purchase

import androidx.paging.PagingData
import java.util.Date
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel
    suspend fun getMonthlySummary(start: Date, end: Date): MonthlyPurchaseSummary
    fun getLatestPurchases(): Flow<PagingData<PurchaseModel>>
}
