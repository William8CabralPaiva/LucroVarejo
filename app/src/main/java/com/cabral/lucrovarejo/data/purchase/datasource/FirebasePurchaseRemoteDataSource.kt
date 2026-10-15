package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import java.util.Date
import javax.inject.Inject

class FirebasePurchaseRemoteDataSource @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreClient: PurchaseFirestoreClient
) : PurchaseRemoteDataSource {
    override suspend fun createPurchase(request: PurchaseRequest): String {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
        return firestoreClient.savePurchase(storeKey, request)
    }

    override suspend fun getPurchasesBetween(
        start: Date,
        end: Date
    ): List<PurchaseSummaryRecord> {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
        return firestoreClient.getPurchasesBetween(storeKey, start, end)
    }

    override suspend fun getLatestPurchases(): List<PurchaseRecord> {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
        return firestoreClient.getLatestPurchases(storeKey, LATEST_PURCHASES_LIMIT)
    }

    private companion object {
        const val LATEST_PURCHASES_LIMIT = 15L
    }
}
