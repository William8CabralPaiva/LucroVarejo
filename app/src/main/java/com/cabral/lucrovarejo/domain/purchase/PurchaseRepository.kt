package com.cabral.lucrovarejo.domain.purchase

interface PurchaseRepository {
    suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel
}
