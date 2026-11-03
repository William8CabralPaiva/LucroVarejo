package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class FirebaseSalesRemoteDataSourceTest {
    @Test
    fun fetchesSalesWithDateRangeCursorAndLimit() = runTest {
        val firestore = FirestoreTree()
        val start = Date(1_000)
        val end = Date(2_000)
        val cursor = SalesCursor(Date(1_500), "purchase-previous")
        val snapshot = mock(QuerySnapshot::class.java)
        val document = PurchaseRecordMapperTest().documentSnapshot(Date(1_700))
        `when`(snapshot.documents).thenReturn(listOf(document))
        firestore.returnSales(snapshot)
        val dataSource = FirebaseSalesRemoteDataSource(FakeAuthRepository("store-1"), firestore.firestore)

        val sales = dataSource.fetchSales(start, end, cursor, 5)

        assertEquals(listOf("purchase-1"), sales.map { it.documentId })
        verify(firestore.beforeCursor).startAfter(cursor.saleDate, cursor.documentId)
        verify(firestore.limited).get()
    }

    @Test
    fun emitsChangesAndRemovesFirestoreListener() = runTest {
        val firestore = FirestoreTree()
        firestore.emitChange()
        val dataSource = FirebaseSalesRemoteDataSource(FakeAuthRepository("store-1"), firestore.firestore)

        dataSource.observeSalesChanges(Date(1_000), Date(2_000)).first()

        verify(firestore.changeRegistration).remove()
    }

    @Test
    fun requiresAnAuthenticatedStore() = runTest {
        val dataSource = FirebaseSalesRemoteDataSource(
            FakeAuthRepository(null),
            mock(FirebaseFirestore::class.java)
        )

        val fetchFailure = runCatching {
            dataSource.fetchSales(null, null, null, 10)
        }.exceptionOrNull()
        val observeFailure = runCatching {
            dataSource.observeSalesChanges(null, null)
        }.exceptionOrNull()

        assertTrue(fetchFailure is PurchaseFailure)
        assertEquals(PurchaseFailure.Reason.UNAUTHENTICATED, (fetchFailure as PurchaseFailure).reason)
        assertTrue(observeFailure is PurchaseFailure)
        assertEquals(PurchaseFailure.Reason.UNAUTHENTICATED, (observeFailure as PurchaseFailure).reason)
    }

    private class FirestoreTree {
        val firestore = mock(FirebaseFirestore::class.java)
        private val stores = mock(CollectionReference::class.java)
        private val store = mock(DocumentReference::class.java)
        private val purchases = mock(CollectionReference::class.java)
        private val afterStart = mock(Query::class.java)
        private val afterEnd = mock(Query::class.java)
        private val sortedByDate = mock(Query::class.java)
        val beforeCursor = mock(Query::class.java)
        val afterCursor = mock(Query::class.java)
        val limited = mock(Query::class.java)
        val changeRegistration = mock(ListenerRegistration::class.java)

        init {
            `when`(firestore.collection("stores")).thenReturn(stores)
            `when`(stores.document("store-1")).thenReturn(store)
            `when`(store.collection("purchases")).thenReturn(purchases)
            `when`(purchases.whereGreaterThanOrEqualTo(
                org.mockito.ArgumentMatchers.eq("sale_date"),
                org.mockito.ArgumentMatchers.any()
            )).thenReturn(afterStart)
            `when`(afterStart.whereLessThan(
                org.mockito.ArgumentMatchers.eq("sale_date"),
                org.mockito.ArgumentMatchers.any()
            )).thenReturn(afterEnd)
            `when`(afterEnd.orderBy("sale_date", Query.Direction.DESCENDING)).thenReturn(sortedByDate)
            `when`(sortedByDate.orderBy(
                org.mockito.ArgumentMatchers.any(com.google.firebase.firestore.FieldPath::class.java),
                org.mockito.ArgumentMatchers.eq(Query.Direction.DESCENDING)
            )).thenReturn(beforeCursor)
            `when`(beforeCursor.startAfter(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
            )).thenReturn(afterCursor)
            `when`(afterCursor.limit(5)).thenReturn(limited)
        }

        fun returnSales(snapshot: QuerySnapshot) {
            `when`(limited.get()).thenReturn(Tasks.forResult(snapshot))
        }

        fun emitChange() {
            doAnswer { invocation ->
                @Suppress("UNCHECKED_CAST")
                (invocation.arguments[0] as EventListener<QuerySnapshot>).onEvent(null, null)
                changeRegistration
            }.`when`(afterEnd).addSnapshotListener(org.mockito.ArgumentMatchers.any())
        }
    }

    private class FakeAuthRepository(
        override val storeKey: String?
    ) : AuthRepository {
        override val hasSession: Boolean
            get() = storeKey != null

        override suspend fun registerStore(storeName: String, email: String, password: String) = Unit

        override suspend fun signIn(storeName: String, password: String) = Unit

        override fun signOut() = Unit
    }
}
