package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FirebasePurchaseRemoteDataSource @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreClient: PurchaseFirestoreClient
) : PurchaseRemoteDataSource {
    override suspend fun createPurchase(request: PurchaseRequest): String =
        firestoreClient.savePurchase(requireStoreKey(), request)

    override fun observePurchasesBetween(
        start: Date,
        end: Date
    ): Flow<List<PurchaseSummaryRecord>> = flow {
        emitAll(firestoreClient.observePurchasesBetween(requireStoreKey(), start, end))
    }

    override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseRecord>> = flow {
        emitAll(firestoreClient.observeLatestPurchases(requireStoreKey(), limit))
    }

    private fun requireStoreKey(): String = authRepository.storeKey
        ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)
}
