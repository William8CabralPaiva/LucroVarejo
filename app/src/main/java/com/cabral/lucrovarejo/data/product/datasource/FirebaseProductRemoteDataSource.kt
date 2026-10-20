package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest
import com.cabral.lucrovarejo.data.product.model.ProductRecord
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    override fun observeProducts(): Flow<List<ProductRecord>> {
        val storeKey = auth.currentUser?.uid
            ?: throw ProductFailure(ProductFailure.Reason.UNAUTHENTICATED)

        return firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PRODUCTS)
            .snapshotFlow { snapshot ->
                snapshot.documents
                    .map { document -> document.toProductRecord() }
                    .sortedByDescending { it.createdAt }
            }
    }

    private fun Query.snapshotFlow(
        map: (QuerySnapshot) -> List<ProductRecord>
    ): Flow<List<ProductRecord>> = callbackFlow {
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

    private fun com.google.firebase.firestore.DocumentSnapshot.toProductRecord(): ProductRecord {
        val request = ProductRequest(
            cod = requireNotNull(getString(FIELD_COD)) {
                "Product $id has no valid '$FIELD_COD' field"
            },
            name = requireNotNull(getString(FIELD_NAME)) {
                "Product $id has no valid '$FIELD_NAME' field"
            },
            cost = Math.toIntExact(requireNotNull(getLong(FIELD_COST)) {
                "Product $id has no valid '$FIELD_COST' field"
            }),
            description = requireNotNull(getString(FIELD_DESCRIPTION)) {
                "Product $id has no valid '$FIELD_DESCRIPTION' field"
            }
        )
        return ProductRecord(
            documentId = id,
            request = request,
            createdAt = getTimestamp(FIELD_CREATED_AT)?.toDate()
        )
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PRODUCTS = "products"
        const val FIELD_COD = "cod"
        const val FIELD_NAME = "name"
        const val FIELD_COST = "cost"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
