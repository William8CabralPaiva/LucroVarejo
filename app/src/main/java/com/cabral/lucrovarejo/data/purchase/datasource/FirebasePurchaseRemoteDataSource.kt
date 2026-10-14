package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.auth.AuthRepository
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
}
