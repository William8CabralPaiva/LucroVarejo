package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest
import com.cabral.lucrovarejo.data.product.model.ProductRecord
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseProductRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductRemoteDataSource {
    override suspend fun createProduct(request: ProductRequest): String {
        val storeKey = auth.currentUser?.uid
            ?: throw ProductFailure(ProductFailure.Reason.UNAUTHENTICATED)
        val productDocument = firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PRODUCTS)
            .document()

        productDocument.set(request.toFirestoreFields()).await()
        return productDocument.id
    }

    override suspend fun getProducts(): List<ProductRecord> {
        val storeKey = auth.currentUser?.uid
            ?: throw ProductFailure(ProductFailure.Reason.UNAUTHENTICATED)
        val snapshot = firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PRODUCTS)
            .get()
            .await()

        return snapshot.documents.map { document ->
            val request = ProductRequest(
                cod = requireNotNull(document.getString(FIELD_COD)) {
                    "Product ${document.id} has no valid '$FIELD_COD' field"
                },
                name = requireNotNull(document.getString(FIELD_NAME)) {
                    "Product ${document.id} has no valid '$FIELD_NAME' field"
                },
                cost = Math.toIntExact(requireNotNull(document.getLong(FIELD_COST)) {
                    "Product ${document.id} has no valid '$FIELD_COST' field"
                }),
                description = requireNotNull(document.getString(FIELD_DESCRIPTION)) {
                    "Product ${document.id} has no valid '$FIELD_DESCRIPTION' field"
                }
            )
            ProductRecord(document.id, request)
        }
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PRODUCTS = "products"
        const val FIELD_COD = "cod"
        const val FIELD_NAME = "name"
        const val FIELD_COST = "cost"
        const val FIELD_DESCRIPTION = "description"
    }
}
