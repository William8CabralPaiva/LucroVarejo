package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.TerminalFeeRecord
import com.cabral.lucrovarejo.data.payment.model.TerminalFeeRequest
import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
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

class FirebaseTerminalFeeRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : TerminalFeeRemoteDataSource {
    override suspend fun addFee(terminalId: String, request: TerminalFeeRequest): String {
        val document = feesCollection(terminalId).document()
        document.set(request.toFirestoreFields()).await()
        return document.id
    }

    override fun observeFees(terminalId: String): Flow<List<TerminalFeeRecord>> =
        feesCollection(terminalId).snapshotFlow { snapshot ->
            snapshot.documents
                .map { document -> document.toTerminalFeeRecord(terminalId) }
                .sortedByDescending { it.request.effectiveFrom }
        }

    override suspend fun getFeesSnapshot(terminalId: String): List<TerminalFeeRecord> =
        feesCollection(terminalId).get().await().documents
            .map { document -> document.toTerminalFeeRecord(terminalId) }

    private fun feesCollection(terminalId: String): CollectionReference {
        val storeKey = auth.currentUser?.uid
            ?: throw PaymentFailure(PaymentFailure.Reason.UNAUTHENTICATED)
        return firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PAYMENT_TERMINALS)
            .document(terminalId)
            .collection(COLLECTION_FEES)
    }

    private fun Query.snapshotFlow(
        map: (QuerySnapshot) -> List<TerminalFeeRecord>
    ): Flow<List<TerminalFeeRecord>> = callbackFlow {
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

    private fun DocumentSnapshot.toTerminalFeeRecord(terminalId: String): TerminalFeeRecord {
        val request = TerminalFeeRequest(
            method = requireNotNull(getString(TerminalFeeRequest.FIELD_METHOD)) {
                "TerminalFee $id has no valid '${TerminalFeeRequest.FIELD_METHOD}' field"
            },
            brandId = getString(TerminalFeeRequest.FIELD_BRAND_ID),
            installments = getLong(TerminalFeeRequest.FIELD_INSTALLMENTS)?.toInt(),
            rateBps = Math.toIntExact(requireNotNull(getLong(TerminalFeeRequest.FIELD_RATE_BPS)) {
                "TerminalFee $id has no valid '${TerminalFeeRequest.FIELD_RATE_BPS}' field"
            }),
            effectiveFrom = requireNotNull(
                getTimestamp(TerminalFeeRequest.FIELD_EFFECTIVE_FROM)?.toDate()
            ) {
                "TerminalFee $id has no valid '${TerminalFeeRequest.FIELD_EFFECTIVE_FROM}' field"
            }
        )
        return TerminalFeeRecord(
            documentId = id,
            terminalId = terminalId,
            request = request,
            createdAt = getTimestamp(TerminalFeeRequest.FIELD_CREATED_AT)?.toDate()
        )
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PAYMENT_TERMINALS = "paymentTerminals"
        const val COLLECTION_FEES = "fees"
    }
}
