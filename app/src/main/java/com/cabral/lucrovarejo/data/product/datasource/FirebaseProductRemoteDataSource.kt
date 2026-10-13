package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest
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
            ?: throw IllegalStateException("A signed-in store is required to create a product.")
        val productDocument = firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PRODUCTS)
            .document()

        productDocument.set(request.toFirestoreFields()).await()
        return productDocument.id
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PRODUCTS = "products"
    }
}
