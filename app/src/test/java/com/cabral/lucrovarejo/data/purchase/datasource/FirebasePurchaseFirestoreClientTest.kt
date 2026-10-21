package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class FirebasePurchaseFirestoreClientTest {
    @Test
    fun savesUpdatesAndDeletesPurchaseDocuments() = runTest {
        val firestore = FirestoreTree()
        val request = request()
        val client = FirebasePurchaseFirestoreClient(firestore.firestore)

        assertEquals("new-purchase", client.savePurchase("store-1", request))
        client.updatePurchase("store-1", "purchase-1", request)
        client.deletePurchase("store-1", "purchase-1")

        verify(firestore.newDocument).set(request.toFirestoreFields())
        verify(firestore.existingDocument).update(request.toFirestoreFields())
        verify(firestore.existingDocument).delete()
    }

    @Test
    fun observesSummaryAndLatestPurchasesAndRemovesListeners() = runTest {
        val firestore = FirestoreTree()
        val summarySnapshot = mock(QuerySnapshot::class.java)
        val summaryDocument = PurchaseRecordMapperTest().documentSnapshot(Date()).also {
            `when`(it.getLong("cost")).thenReturn(1200)
            `when`(it.getLong("sale_price")).thenReturn(1500)
            `when`(it.getLong("quantity")).thenReturn(2)
        }
        val latestSnapshot = mock(QuerySnapshot::class.java)
        `when`(summarySnapshot.documents).thenReturn(listOf(summaryDocument))
        val latestDocument = PurchaseRecordMapperTest().documentSnapshot(Date(1_735_689_600_000))
        `when`(latestSnapshot.documents).thenReturn(listOf(latestDocument))
        firestore.emitSummary(summarySnapshot)
        firestore.emitLatest(latestSnapshot)
        val client = FirebasePurchaseFirestoreClient(firestore.firestore)

        val summary = client.observePurchasesBetween("store-1", Date(1_000), Date(2_000)).first()
        val latest = client.observeLatestPurchases("store-1", 4).first()

        assertEquals(1200, summary.single().cost)
        assertEquals(1500, summary.single().salePrice)
        assertEquals(2, summary.single().quantity)
        assertEquals("purchase-1", latest.single().documentId)
        verify(firestore.summaryRegistration).remove()
        verify(firestore.latestRegistration).remove()
    }

    private fun request() = PurchaseRequest(
        cod = "A-1",
        name = "Produto",
        cost = 1200,
        description = "Descricao",
        salePrice = 1500,
        quantity = 2,
        saleDate = Date(1_735_689_600_000),
        paymentMethod = PaymentMethods.PIX
    )

    private class FirestoreTree {
        val firestore = mock(FirebaseFirestore::class.java)
        private val stores = mock(CollectionReference::class.java)
        private val store = mock(DocumentReference::class.java)
        private val purchases = mock(CollectionReference::class.java)
        val newDocument = mock(DocumentReference::class.java)
        val existingDocument = mock(DocumentReference::class.java)
        private val summaryAfterStart = mock(Query::class.java)
        private val summaryQuery = mock(Query::class.java)
        private val latestByDate = mock(Query::class.java)
        private val latestById = mock(Query::class.java)
        private val latestQuery = mock(Query::class.java)
        val summaryRegistration = mock(ListenerRegistration::class.java)
        val latestRegistration = mock(ListenerRegistration::class.java)

        init {
            `when`(firestore.collection("stores")).thenReturn(stores)
            `when`(stores.document("store-1")).thenReturn(store)
            `when`(store.collection("purchases")).thenReturn(purchases)
            `when`(purchases.document()).thenReturn(newDocument)
            `when`(purchases.document("purchase-1")).thenReturn(existingDocument)
            `when`(newDocument.id).thenReturn("new-purchase")
            `when`(newDocument.set(org.mockito.ArgumentMatchers.anyMap<String, Any>()))
                .thenReturn(Tasks.forResult<Void>(null))
            `when`(existingDocument.update(org.mockito.ArgumentMatchers.anyMap<String, Any>()))
                .thenReturn(Tasks.forResult<Void>(null))
            `when`(existingDocument.delete()).thenReturn(Tasks.forResult<Void>(null))
            `when`(purchases.whereGreaterThanOrEqualTo(
                org.mockito.ArgumentMatchers.eq("sale_date"),
                org.mockito.ArgumentMatchers.any()
            )).thenReturn(summaryAfterStart)
            `when`(summaryAfterStart.whereLessThan(
                org.mockito.ArgumentMatchers.eq("sale_date"),
                org.mockito.ArgumentMatchers.any()
            )).thenReturn(summaryQuery)
            `when`(purchases.orderBy("sale_date", Query.Direction.DESCENDING)).thenReturn(latestByDate)
            `when`(latestByDate.orderBy(FieldPath.documentId(), Query.Direction.DESCENDING))
                .thenReturn(latestById)
            `when`(latestById.limit(4)).thenReturn(latestQuery)
        }

        fun emitSummary(snapshot: QuerySnapshot) {
            doAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                (invocation.arguments[0] as EventListener<QuerySnapshot>).onEvent(snapshot, null)
                summaryRegistration
            }.`when`(summaryQuery).addSnapshotListener(org.mockito.ArgumentMatchers.any())
        }

        fun emitLatest(snapshot: QuerySnapshot) {
            doAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                (invocation.arguments[0] as EventListener<QuerySnapshot>).onEvent(snapshot, null)
                latestRegistration
            }.`when`(latestQuery).addSnapshotListener(org.mockito.ArgumentMatchers.any())
        }
    }
}
