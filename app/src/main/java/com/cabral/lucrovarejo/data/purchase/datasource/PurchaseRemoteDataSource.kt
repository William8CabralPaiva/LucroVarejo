package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest

interface PurchaseRemoteDataSource {
    suspend fun createPurchase(request: PurchaseRequest): String
}

interface PurchaseFirestoreClient {
    suspend fun savePurchase(storeKey: String, request: PurchaseRequest): String
}
