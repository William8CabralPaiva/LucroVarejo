package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import java.util.Date
import kotlinx.coroutines.flow.Flow

interface PurchaseRemoteDataSource {
    suspend fun createPurchase(request: PurchaseRequest): String
    fun observePurchasesBetween(start: Date, end: Date): Flow<List<PurchaseSummaryRecord>>
    fun observeLatestPurchases(limit: Int): Flow<List<PurchaseRecord>>
}

interface PurchaseFirestoreClient {
    suspend fun savePurchase(storeKey: String, request: PurchaseRequest): String
    fun observePurchasesBetween(
        storeKey: String,
        start: Date,
        end: Date
    ): Flow<List<PurchaseSummaryRecord>>
    fun observeLatestPurchases(storeKey: String, limit: Int): Flow<List<PurchaseRecord>>
}
