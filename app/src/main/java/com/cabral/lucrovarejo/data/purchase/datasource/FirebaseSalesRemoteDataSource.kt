package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseSalesRemoteDataSource @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : SalesRemoteDataSource {
    override suspend fun fetchSales(
        dateStart: Date?,
        dateEnd: Date?,
        after: SalesCursor?,
        limit: Int
    ): List<PurchaseRecord> {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)

        var query = salesQuery(storeKey)
        dateStart?.let { query = query.whereGreaterThanOrEqualTo(FIELD_SALE_DATE, it) }
        dateEnd?.let { query = query.whereLessThan(FIELD_SALE_DATE, it) }
        query = query
            .orderBy(FIELD_SALE_DATE, Query.Direction.DESCENDING)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
        after?.let { query = query.startAfter(it.saleDate, it.documentId) }

        return query.limit(limit.toLong()).get().await().documents.map { it.toPurchaseRecord() }
    }

    override fun observeSalesChanges(dateStart: Date?, dateEnd: Date?): Flow<Unit> {
        val storeKey = authRepository.storeKey
            ?: throw PurchaseFailure(PurchaseFailure.Reason.UNAUTHENTICATED)

        var query = salesQuery(storeKey)
        dateStart?.let { query = query.whereGreaterThanOrEqualTo(FIELD_SALE_DATE, it) }
        dateEnd?.let { query = query.whereLessThan(FIELD_SALE_DATE, it) }

        return callbackFlow {
            // Este listener vincula a tela de Vendas à coleção "purchases": cada alteração no
            // Firestore envia uma emissão ao Flow, que faz o SalesViewModel recarregar a lista.
            val registration = query.addSnapshotListener { _, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(Unit)
            }
            awaitClose { registration.remove() }
        }
    }

    private fun salesQuery(storeKey: String): Query = firestore.collection(COLLECTION_STORES)
        .document(storeKey)
        .collection(COLLECTION_PURCHASES)

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PURCHASES = "purchases"
        const val FIELD_SALE_DATE = "sale_date"
    }
}
