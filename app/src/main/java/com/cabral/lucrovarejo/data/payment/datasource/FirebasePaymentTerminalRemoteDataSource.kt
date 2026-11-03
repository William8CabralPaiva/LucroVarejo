package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRecord
import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRequest
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

class FirebasePaymentTerminalRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PaymentTerminalRemoteDataSource {
    override suspend fun createTerminal(request: PaymentTerminalRequest): String {
        val document = terminalsCollection().document()
        document.set(request.toFirestoreFields()).await()
        return document.id
    }

    override suspend fun updateTerminal(key: String, request: PaymentTerminalRequest) {
        terminalDocument(key).update(request.toFirestoreUpdateFields()).await()
    }

    override suspend fun setTerminalActive(key: String, active: Boolean) {
        terminalDocument(key).update(PaymentTerminalRequest.FIELD_ACTIVE, active).await()
    }

    override fun observeTerminals(): Flow<List<PaymentTerminalRecord>> =
        terminalsCollection().snapshotFlow { snapshot ->
            snapshot.documents
                .map { document -> document.toPaymentTerminalRecord() }
                .sortedBy { it.request.name.lowercase() }
        }

    override fun observeTerminal(key: String): Flow<PaymentTerminalRecord?> = callbackFlow {
        val registration = terminalDocument(key).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val record = snapshot?.takeIf { it.exists() }?.toPaymentTerminalRecord()
            trySend(record)
        }
        awaitClose { registration.remove() }
    }

    private fun terminalsCollection() =
        storeDocument().collection(COLLECTION_PAYMENT_TERMINALS)

    private fun terminalDocument(key: String): DocumentReference =
        terminalsCollection().document(key)

    private fun storeDocument(): DocumentReference {
        val storeKey = auth.currentUser?.uid
            ?: throw PaymentFailure(PaymentFailure.Reason.UNAUTHENTICATED)
        return firestore.collection(COLLECTION_STORES).document(storeKey)
    }

    private fun Query.snapshotFlow(
        map: (QuerySnapshot) -> List<PaymentTerminalRecord>
    ): Flow<List<PaymentTerminalRecord>> = callbackFlow {
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

    private fun DocumentSnapshot.toPaymentTerminalRecord(): PaymentTerminalRecord {
        val request = PaymentTerminalRequest(
            name = requireNotNull(getString(PaymentTerminalRequest.FIELD_NAME)) {
                "PaymentTerminal $id has no valid '${PaymentTerminalRequest.FIELD_NAME}' field"
            },
            active = getBoolean(PaymentTerminalRequest.FIELD_ACTIVE) ?: true,
            enabledMethods = (get(PaymentTerminalRequest.FIELD_ENABLED_METHODS) as? List<*>)
                ?.filterIsInstance<String>().orEmpty(),
            brands = (get(PaymentTerminalRequest.FIELD_BRANDS) as? List<*>)
                ?.filterIsInstance<String>().orEmpty()
        )
        return PaymentTerminalRecord(
            documentId = id,
            request = request,
            createdAt = getTimestamp(PaymentTerminalRequest.FIELD_CREATED_AT)?.toDate()
        )
    }

    private companion object {
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_PAYMENT_TERMINALS = "paymentTerminals"
    }
}
