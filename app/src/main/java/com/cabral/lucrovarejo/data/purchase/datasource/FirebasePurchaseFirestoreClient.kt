package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.google.firebase.firestore.FirebaseFirestore
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

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PURCHASES = "purchases"
    }
}
