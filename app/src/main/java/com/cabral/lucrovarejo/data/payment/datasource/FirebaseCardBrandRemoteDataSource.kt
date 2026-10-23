package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.CardBrandRecord
import com.cabral.lucrovarejo.data.payment.model.CardBrandRequest
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseCardBrandRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : CardBrandRemoteDataSource {
    override suspend fun createCardBrand(request: CardBrandRequest): String {
        val document = cardBrandsCollection().document()
        document.set(request.toFirestoreFields()).await()
        return document.id
    }

    override suspend fun renameCardBrand(key: String, name: String) {
        cardBrandDocument(key).update(CardBrandRequest.FIELD_NAME, name).await()
    }

    override suspend fun setCardBrandActive(key: String, active: Boolean) {
        cardBrandDocument(key).update(CardBrandRequest.FIELD_ACTIVE, active).await()
    }

    override fun observeCardBrands(): Flow<List<CardBrandRecord>> =
        cardBrandsCollection().snapshotFlow { snapshot ->
            snapshot.documents
                .map { document -> document.toCardBrandRecord() }
                .sortedBy { it.request.name.lowercase() }
        }

    private fun cardBrandsCollection() =
        storeDocument().collection(COLLECTION_CARD_BRANDS)

    private fun cardBrandDocument(key: String): DocumentReference =
        cardBrandsCollection().document(key)

    private fun storeDocument(): DocumentReference {
        val storeKey = auth.currentUser?.uid
            ?: throw PaymentFailure(PaymentFailure.Reason.UNAUTHENTICATED)
        return firestore.collection(COLLECTION_STORES).document(storeKey)
    }

    private fun Query.snapshotFlow(
        map: (QuerySnapshot) -> List<CardBrandRecord>
    ): Flow<List<CardBrandRecord>> = callbackFlow {
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

    private fun DocumentSnapshot.toCardBrandRecord(): CardBrandRecord {
        val request = CardBrandRequest(
            name = requireNotNull(getString(CardBrandRequest.FIELD_NAME)) {
                "CardBrand $id has no valid '${CardBrandRequest.FIELD_NAME}' field"
            },
            active = getBoolean(CardBrandRequest.FIELD_ACTIVE) ?: true
        )
        return CardBrandRecord(
            documentId = id,
            request = request,
            createdAt = getTimestamp(CardBrandRequest.FIELD_CREATED_AT)?.toDate()
        )
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_CARD_BRANDS = "cardBrands"
    }
}
