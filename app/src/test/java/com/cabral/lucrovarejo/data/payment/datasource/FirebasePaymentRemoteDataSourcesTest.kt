package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.cabral.lucrovarejo.data.payment.model.PaymentMachineRequest
import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRequest
import com.cabral.lucrovarejo.domain.common.DataFailure
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FirebasePaymentRemoteDataSourcesTest {
    private val firestore = mock(FirebaseFirestore::class.java)
    private val stores = mock(CollectionReference::class.java)
    private val store = mock(DocumentReference::class.java)
    private val terminals = mock(CollectionReference::class.java)
    private val terminal = mock(DocumentReference::class.java)
    private val newTerminal = mock(DocumentReference::class.java)
    private val tariffs = mock(CollectionReference::class.java)
    private val newTariff = mock(DocumentReference::class.java)
    private val registration = mock(ListenerRegistration::class.java)

    init {
        `when`(firestore.collection(FirestoreCollections.STORES)).thenReturn(stores)
        `when`(stores.document("store-1")).thenReturn(store)
        `when`(store.collection("paymentTerminals")).thenReturn(terminals)
        `when`(terminals.document()).thenReturn(newTerminal)
        `when`(terminals.document("t1")).thenReturn(terminal)
        `when`(newTerminal.id).thenReturn("new-terminal")
        `when`(newTerminal.set(any())).thenReturn(Tasks.forResult(null))
        `when`(terminal.update(any<Map<String, Any>>())).thenReturn(Tasks.forResult(null))
        `when`(terminal.update(eq("active"), eq(false))).thenReturn(Tasks.forResult(null))
        `when`(terminal.collection("tariffs")).thenReturn(tariffs)
        `when`(tariffs.document()).thenReturn(newTariff)
        `when`(newTariff.id).thenReturn("new-tariff")
        `when`(newTariff.set(any())).thenReturn(Tasks.forResult(null))
    }

    private val terminalSource get() = FirebasePaymentTerminalRemoteDataSource({ "store-1" }, firestore)
    private val machineSource get() = FirebasePaymentMachineRemoteDataSource({ "store-1" }, firestore)

    private fun terminalDoc(id: String, name: String?): DocumentSnapshot {
        val doc = mock(DocumentSnapshot::class.java)
        `when`(doc.id).thenReturn(id)
        `when`(doc.exists()).thenReturn(true)
        `when`(doc.getString("name")).thenReturn(name)
        `when`(doc.getBoolean("active")).thenReturn(null)
        `when`(doc.get("enabledMethods")).thenReturn(listOf("PIX", 1))
        `when`(doc.get("brands")).thenReturn(listOf("Visa"))
        `when`(doc.getTimestamp("createdAt")).thenReturn(Timestamp(Date(5_000)))
        return doc
    }

    private fun tariffDoc(id: String, method: String?, rate: Long?, from: Long?): DocumentSnapshot {
        val doc = mock(DocumentSnapshot::class.java)
        `when`(doc.id).thenReturn(id)
        `when`(doc.getString("method")).thenReturn(method)
        `when`(doc.getString("brandId")).thenReturn("visa")
        `when`(doc.getLong("installments")).thenReturn(3L)
        `when`(doc.getLong("rateBps")).thenReturn(rate)
        `when`(doc.getTimestamp("effectiveFrom")).thenReturn(from?.let { Timestamp(Date(it)) })
        return doc
    }

    private fun <T> emit(target: Any, snapshot: T?, error: FirebaseFirestoreException? = null) {
        val answer = { invocation: org.mockito.invocation.InvocationOnMock ->
            @Suppress("UNCHECKED_CAST")
            (invocation.arguments[0] as EventListener<T>).onEvent(snapshot, error)
            registration
        }
        when (target) {
            is CollectionReference -> doAnswer(answer).`when`(target).addSnapshotListener(any())
            is DocumentReference -> doAnswer(answer).`when`(target).addSnapshotListener(any())
        }
    }

    @Test
    fun terminalCreateUpdateAndArchive() = runTest {
        val request = PaymentTerminalRequest("Stone", true, listOf("PIX"), listOf("Visa"))

        assertEquals("new-terminal", terminalSource.createTerminal(request))
        terminalSource.updateTerminal("t1", request)
        terminalSource.setTerminalActive("t1", false)

        verify(newTerminal).set(any())
        verify(terminal).update(request.toFirestoreUpdateFields())
        verify(terminal).update(eq("active"), eq(false))
    }

    @Test
    fun terminalRequiresStore() = runTest {
        val source = FirebasePaymentTerminalRemoteDataSource({ null }, firestore)
        try {
            source.setTerminalActive("t1", true)
            fail()
        } catch (failure: DataFailure) {
            assertEquals(DataFailure.Reason.UNAUTHENTICATED, failure.reason)
        }
    }

    @Test
    fun observeTerminalsSortsByNameAndDefaultsFields() = runTest {
        val snapshot = mock(QuerySnapshot::class.java)
        val docs = listOf(terminalDoc("b", "zeta"), terminalDoc("a", "Alfa"))
        `when`(snapshot.documents).thenReturn(docs)
        emit(terminals, snapshot)

        val records = terminalSource.observeTerminals().first()

        assertEquals(listOf("a", "b"), records.map { it.documentId })
        assertTrue(records.first().request.active)
        assertEquals(listOf("PIX"), records.first().request.enabledMethods)
        assertEquals(Date(5_000), records.first().createdAt)
        verify(registration).remove()
    }

    @Test
    fun observeTerminalsPropagatesListenerErrorAndInvalidDocs() = runTest {
        val error = FirebaseFirestoreException("x", FirebaseFirestoreException.Code.UNAVAILABLE)
        emit<QuerySnapshot>(terminals, null, error)
        try {
            terminalSource.observeTerminals().first()
            fail()
        } catch (e: FirebaseFirestoreException) {
            assertEquals(error, e)
        }

        val snapshot = mock(QuerySnapshot::class.java)
        val invalid = terminalDoc("x", null)
        `when`(snapshot.documents).thenReturn(listOf(invalid))
        emit(terminals, snapshot)
        try {
            terminalSource.observeTerminals().first()
            fail()
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun observeTerminalEmitsRecordOrNull() = runTest {
        val missing = mock(DocumentSnapshot::class.java)
        `when`(missing.exists()).thenReturn(false)

        emit(terminal, terminalDoc("t1", "Stone"))
        assertEquals("Stone", terminalSource.observeTerminal("t1").first()?.request?.name)

        emit(terminal, missing)
        assertNull(terminalSource.observeTerminal("t1").first())

        val error = FirebaseFirestoreException("x", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        emit<DocumentSnapshot>(terminal, null, error)
        try {
            terminalSource.observeTerminal("t1").first()
            fail()
        } catch (e: FirebaseFirestoreException) {
            assertEquals(error, e)
        }
    }

    @Test
    fun addTariffWritesDocument() = runTest {
        val request = PaymentMachineRequest("PIX", null, null, 249, Date(1_000))

        assertEquals("new-tariff", machineSource.addTariff("t1", request))
        verify(newTariff).set(any())
    }

    @Test
    fun tariffsRequireStore() = runTest {
        val source = FirebasePaymentMachineRemoteDataSource({ null }, firestore)
        try {
            source.getTariffsSnapshot("t1")
            fail()
        } catch (failure: DataFailure) {
            assertEquals(DataFailure.Reason.UNAUTHENTICATED, failure.reason)
        }
    }

    @Test
    fun getTariffsSnapshotMapsDocuments() = runTest {
        val snapshot = mock(QuerySnapshot::class.java)
        val doc = tariffDoc("x", "DEBIT", 249, 1_000)
        `when`(snapshot.documents).thenReturn(listOf(doc))
        `when`(tariffs.get()).thenReturn(Tasks.forResult(snapshot))

        val record = machineSource.getTariffsSnapshot("t1").single()

        assertEquals("t1", record.terminalId)
        assertEquals("DEBIT", record.request.method)
        assertEquals(249, record.request.rateBps)
        assertEquals(3, record.request.installments)
        assertEquals("visa", record.request.brandId)
    }

    @Test
    fun observeTariffsSortsByEffectiveDateDescendingAndRejectsInvalid() = runTest {
        val snapshot = mock(QuerySnapshot::class.java)
        val docs = listOf(tariffDoc("old", "PIX", 100, 1_000), tariffDoc("new", "PIX", 200, 2_000))
        val invalidDocs = listOf(
            tariffDoc("a", null, 1, 1_000),
            tariffDoc("b", "PIX", null, 1_000),
            tariffDoc("c", "PIX", 1, null)
        )
        `when`(snapshot.documents).thenReturn(docs)
        emit(tariffs, snapshot)

        assertEquals(listOf("new", "old"), machineSource.observeTariffs("t1").first().map { it.documentId })

        for (invalid in invalidDocs) {
            `when`(snapshot.documents).thenReturn(listOf(invalid))
            try {
                machineSource.observeTariffs("t1").first()
                fail()
            } catch (e: IllegalArgumentException) {
            }
        }
    }
}
