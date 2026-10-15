package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebasePurchaseFirestoreClient @Inject constructor(
    private val firestore: FirebaseFirestore
) : PurchaseFirestoreClient {
    override suspend fun savePurchase(storeKey: String, request: PurchaseRequest): String {
        val purchaseDocument = firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PURCHASES)
            .document()

        purchaseDocument.set(request.toFirestoreFields()).await()
        return purchaseDocument.id
    }

    override suspend fun getPurchasesBetween(
        storeKey: String,
        start: Date,
        end: Date
    ): List<PurchaseSummaryRecord> {
        val snapshot = firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PURCHASES)
            .whereGreaterThanOrEqualTo(FIELD_SALE_DATE, start)
            .whereLessThan(FIELD_SALE_DATE, end)
            .get()
            .await()

        return snapshot.documents.map { document ->
            PurchaseSummaryRecord(
                cost = requireNotNull(document.getLong(FIELD_COST)) {
                    "Purchase ${document.id} has no valid '$FIELD_COST' field"
                },
                salePrice = requireNotNull(document.getLong(FIELD_SALE_PRICE)) {
                    "Purchase ${document.id} has no valid '$FIELD_SALE_PRICE' field"
                },
                quantity = requireNotNull(document.getLong(FIELD_QUANTITY)) {
                    "Purchase ${document.id} has no valid '$FIELD_QUANTITY' field"
                }
            )
        }
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PURCHASES = "purchases"
        const val FIELD_COST = "cost"
        const val FIELD_SALE_PRICE = "sale_price"
        const val FIELD_QUANTITY = "quantity"
        const val FIELD_SALE_DATE = "sale_date"
    }
}
