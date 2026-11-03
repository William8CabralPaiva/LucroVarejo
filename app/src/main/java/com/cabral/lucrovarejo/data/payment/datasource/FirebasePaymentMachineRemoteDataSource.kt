package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.PaymentMachineRecord
import com.cabral.lucrovarejo.data.payment.model.PaymentMachineRequest
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

class FirebasePaymentMachineRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PaymentMachineRemoteDataSource {
    override suspend fun addTariff(terminalId: String, request: PaymentMachineRequest): String {
        val document = tariffsCollection(terminalId).document()
        document.set(request.toFirestoreFields()).await()
        return document.id
    }

    override fun observeTariffs(terminalId: String): Flow<List<PaymentMachineRecord>> =
        tariffsCollection(terminalId).snapshotFlow { snapshot ->
            snapshot.documents
                .map { document -> document.toPaymentMachineRecord(terminalId) }
                .sortedByDescending { it.request.effectiveFrom }
        }

    override suspend fun getTariffsSnapshot(terminalId: String): List<PaymentMachineRecord> =
        tariffsCollection(terminalId).get().await().documents
            .map { document -> document.toPaymentMachineRecord(terminalId) }

    private fun tariffsCollection(terminalId: String): CollectionReference {
        val storeKey = auth.currentUser?.uid
            ?: throw PaymentFailure(PaymentFailure.Reason.UNAUTHENTICATED)
        return firestore.collection(COLLECTION_STORES)
            .document(storeKey)
            .collection(COLLECTION_PAYMENT_TERMINALS)
            .document(terminalId)
            .collection(COLLECTION_TARIFFS)
    }

    private fun Query.snapshotFlow(
        map: (QuerySnapshot) -> List<PaymentMachineRecord>
    ): Flow<List<PaymentMachineRecord>> = callbackFlow {
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

    private fun DocumentSnapshot.toPaymentMachineRecord(terminalId: String): PaymentMachineRecord {
        val request = PaymentMachineRequest(
            method = requireNotNull(getString(PaymentMachineRequest.FIELD_METHOD)) {
                "PaymentMachine $id has no valid '${PaymentMachineRequest.FIELD_METHOD}' field"
            },
            brandId = getString(PaymentMachineRequest.FIELD_BRAND_ID),
            installments = getLong(PaymentMachineRequest.FIELD_INSTALLMENTS)?.toInt(),
            rateBps = Math.toIntExact(requireNotNull(getLong(PaymentMachineRequest.FIELD_RATE_BPS)) {
                "PaymentMachine $id has no valid '${PaymentMachineRequest.FIELD_RATE_BPS}' field"
            }),
            effectiveFrom = requireNotNull(
                getTimestamp(PaymentMachineRequest.FIELD_EFFECTIVE_FROM)?.toDate()
            ) {
                "PaymentMachine $id has no valid '${PaymentMachineRequest.FIELD_EFFECTIVE_FROM}' field"
            }
        )
        return PaymentMachineRecord(
            documentId = id,
            terminalId = terminalId,
            request = request,
            createdAt = getTimestamp(PaymentMachineRequest.FIELD_CREATED_AT)?.toDate()
        )
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PAYMENT_TERMINALS = "paymentTerminals"
        const val COLLECTION_TARIFFS = "tariffs"
    }
}
