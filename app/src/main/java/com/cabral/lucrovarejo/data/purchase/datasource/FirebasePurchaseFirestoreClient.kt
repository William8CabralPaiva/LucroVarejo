package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebasePurchaseFirestoreClient @Inject constructor(
    private val firestore: FirebaseFirestore
) : PurchaseFirestoreClient {
    override suspend fun savePurchase(storeKey: String, request: PurchaseRequest): String {
        val purchaseDocument = firestore.collection(FirestoreCollections.STORES)
            .document(storeKey)
            .collection(FirestoreCollections.PURCHASES)
            .document()

        purchaseDocument.set(request.toFirestoreFields()).await()
        return purchaseDocument.id
    }

    override suspend fun updatePurchase(storeKey: String, key: String, request: PurchaseRequest) {
        purchasesCollection(storeKey).document(key).update(request.toFirestoreFields()).await()
    }

    override suspend fun deletePurchase(storeKey: String, key: String) {
        purchasesCollection(storeKey).document(key).delete().await()
    }

    override fun observePurchasesBetween(
        storeKey: String,
        start: Date,
        end: Date
    ): Flow<List<PurchaseSummaryRecord>> = purchasesCollection(storeKey)
        .whereGreaterThanOrEqualTo(FIELD_SALE_DATE, start)
        .whereLessThan(FIELD_SALE_DATE, end)
        .snapshotFlow { snapshot ->
            snapshot.documents.map { document ->
                PurchaseSummaryRecord(
                    cost = requireNotNull(document.getLong(FIELD_COST)) {
                        "Purchase ${document.id} has no valid '$FIELD_COST' field"
                    },
                    salePrice = requireNotNull(document.getLong(FIELD_SALE_PRICE)) {
                        "Purchase ${document.id} has no valid '$FIELD_SALE_PRICE' field"
                    },
                    quantity = requireNotNull(document.getLong(FIELD_QUANTITY)) {
                        "Purchase ${document.id} has no valid '$FIELD_QUANTITY' field"
                    },
                    // Vendas antigas não têm tarifa salva.
                    tariffAmountCents = document.getLong(FIELD_TARIFF_AMOUNT_CENTS) ?: 0L
                )
            }
        }

    override fun observeLatestPurchases(storeKey: String, limit: Int): Flow<List<PurchaseRecord>> =
        purchasesCollection(storeKey)
            .orderBy(FIELD_SALE_DATE, Query.Direction.DESCENDING)
            .orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshotFlow { snapshot ->
                snapshot.documents.map { it.toPurchaseRecord() }
            }

    private fun purchasesCollection(storeKey: String) = firestore.collection(FirestoreCollections.STORES)
        .document(storeKey)
        .collection(FirestoreCollections.PURCHASES)

    // Converte o snapshot listener em Flow. Cada alteracao na colecao (inclusive uma venda
    // cadastrada em outra tela) gera uma nova emissao; awaitClose remove o listener.
    private fun <T> Query.snapshotFlow(map: (QuerySnapshot) -> T): Flow<T> = callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            try {
                trySend(map(snapshot))
            } catch (failure: Exception) {
                close(failure)
            }
        }
        awaitClose { registration.remove() }
    }
    private companion object {
        const val FIELD_COST = "cost"
        const val FIELD_SALE_PRICE = "sale_price"
        const val FIELD_QUANTITY = "quantity"
        const val FIELD_TARIFF_AMOUNT_CENTS = "tariff_amount_cents"
        const val FIELD_SALE_DATE = "sale_date"
    }
}