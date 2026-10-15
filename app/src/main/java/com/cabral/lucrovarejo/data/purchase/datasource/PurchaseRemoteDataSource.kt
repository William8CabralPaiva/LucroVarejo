package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import java.util.Date

interface PurchaseRemoteDataSource {
    suspend fun createPurchase(request: PurchaseRequest): String
    suspend fun getPurchasesBetween(start: Date, end: Date): List<PurchaseSummaryRecord>
    suspend fun getLatestPurchases(): List<PurchaseRecord>
}

interface PurchaseFirestoreClient {
    suspend fun savePurchase(storeKey: String, request: PurchaseRequest): String
    suspend fun getPurchasesBetween(
        storeKey: String,
        start: Date,
        end: Date
    ): List<PurchaseSummaryRecord>
    suspend fun getLatestPurchases(storeKey: String, limit: Long): List<PurchaseRecord>
}
