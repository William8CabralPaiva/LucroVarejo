package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    override suspend fun getLatestPurchases(
        storeKey: String,
        limit: Long
    ): List<PurchaseRecord> {
        val snapshot = firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PURCHASES)
            .orderBy(FIELD_SALE_DATE, Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snapshot.documents.map { document ->
            PurchaseRecord(
                documentId = document.id,
                cod = requireNotNull(document.getString(FIELD_COD)) {
                    "Purchase ${document.id} has no valid '$FIELD_COD' field"
                },
                name = requireNotNull(document.getString(FIELD_NAME)) {
                    "Purchase ${document.id} has no valid '$FIELD_NAME' field"
                },
                cost = Math.toIntExact(requireNotNull(document.getLong(FIELD_COST)) {
                    "Purchase ${document.id} has no valid '$FIELD_COST' field"
                }),
                description = requireNotNull(document.getString(FIELD_DESCRIPTION)) {
                    "Purchase ${document.id} has no valid '$FIELD_DESCRIPTION' field"
                },
                salePrice = Math.toIntExact(requireNotNull(document.getLong(FIELD_SALE_PRICE)) {
                    "Purchase ${document.id} has no valid '$FIELD_SALE_PRICE' field"
                }),
                quantity = Math.toIntExact(requireNotNull(document.getLong(FIELD_QUANTITY)) {
                    "Purchase ${document.id} has no valid '$FIELD_QUANTITY' field"
                }),
                saleDate = requireNotNull(document.getTimestamp(FIELD_SALE_DATE)) {
                    "Purchase ${document.id} has no valid '$FIELD_SALE_DATE' field"
                }.toDate(),
                paymentMethod = requireNotNull(document.getString(FIELD_PAYMENT_METHOD)) {
                    "Purchase ${document.id} has no valid '$FIELD_PAYMENT_METHOD' field"
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
        const val FIELD_COD = "cod"
        const val FIELD_NAME = "name"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_PAYMENT_METHOD = "payment_method"
    }
}
