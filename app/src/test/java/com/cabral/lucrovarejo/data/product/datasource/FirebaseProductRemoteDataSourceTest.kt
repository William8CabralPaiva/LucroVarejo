package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class FirebaseProductRemoteDataSourceTest {
    @Test
    fun createsUpdatesAndDeletesProductsForTheAuthenticatedStore() = runTest {
        val firebase = firebaseWithAuthenticatedStore()
        val dataSource = FirebaseProductRemoteDataSource(firebase.auth, firebase.firestore)
        val request = ProductRequest("A-1", "Produto", 1234, "Descricao")

        assertEquals("product-1", dataSource.createProduct(request))
        dataSource.updateProduct("product-1", request)
        dataSource.deleteProduct("product-1")

        verify(firebase.newDocument).set(request.toFirestoreFields())
        verify(firebase.existingDocument).update(request.toFirestoreUpdateFields())
        verify(firebase.existingDocument).delete()
    }

    @Test
    fun requiresAuthenticationForEveryProductOperation() = runTest {
        val dataSource = FirebaseProductRemoteDataSource(
            mock(FirebaseAuth::class.java),
            mock(FirebaseFirestore::class.java)
        )
        val request = ProductRequest("A-1", "Produto", 1234, "Descricao")

        assertUnauthenticated { dataSource.createProduct(request) }
        assertUnauthenticated { dataSource.updateProduct("product-1", request) }
        assertUnauthenticated { dataSource.deleteProduct("product-1") }
        assertTrue(runCatching { dataSource.observeProducts() }.exceptionOrNull() is ProductFailure)
    }

    @Test
    fun observesAndSortsProductSnapshotsByCreationDate() = runTest {
        val firebase = firebaseWithAuthenticatedStore()
        val newest = productDocument("newest", "Produto novo", Date(2_000))
        val oldest = productDocument("oldest", "Produto antigo", Date(1_000))
        val snapshot = mock(QuerySnapshot::class.java)
        val registration = mock(ListenerRegistration::class.java)
        `when`(snapshot.documents).thenReturn(listOf(oldest, newest))
        doAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            val listener = invocation.arguments[0] as EventListener<QuerySnapshot>
            listener.onEvent(snapshot, null)
            registration
        }.`when`(firebase.products).addSnapshotListener(org.mockito.ArgumentMatchers.any())

        val records = FirebaseProductRemoteDataSource(firebase.auth, firebase.firestore)
            .observeProducts()
            .first()

        assertEquals(listOf("newest", "oldest"), records.map { it.documentId })
        verify(registration).remove()
    }

    private suspend fun assertUnauthenticated(operation: suspend () -> Unit) {
        val failure = runCatching { operation() }.exceptionOrNull()
        assertTrue(failure is ProductFailure)
        assertEquals(ProductFailure.Reason.UNAUTHENTICATED, (failure as ProductFailure).reason)
    }

    private fun firebaseWithAuthenticatedStore(): FirebaseObjects {
        val auth = mock(FirebaseAuth::class.java)
        val user = mock(FirebaseUser::class.java)
        val firestore = mock(FirebaseFirestore::class.java)
        val stores = mock(CollectionReference::class.java)
        val store = mock(DocumentReference::class.java)
        val products = mock(CollectionReference::class.java)
        val newDocument = mock(DocumentReference::class.java)
        val existingDocument = mock(DocumentReference::class.java)

        `when`(auth.currentUser).thenReturn(user)
        `when`(user.uid).thenReturn("store-uid")
        `when`(firestore.collection("stores")).thenReturn(stores)
        `when`(stores.document("store-uid")).thenReturn(store)
        `when`(store.collection("products")).thenReturn(products)
        `when`(products.document()).thenReturn(newDocument)
        `when`(products.document("product-1")).thenReturn(existingDocument)
        `when`(newDocument.id).thenReturn("product-1")
        `when`(newDocument.set(org.mockito.ArgumentMatchers.anyMap<String, Any>()))
            .thenReturn(Tasks.forResult<Void>(null))
        `when`(existingDocument.update(org.mockito.ArgumentMatchers.anyMap<String, Any>()))
            .thenReturn(Tasks.forResult<Void>(null))
        `when`(existingDocument.delete()).thenReturn(Tasks.forResult<Void>(null))

        return FirebaseObjects(auth, firestore, products, newDocument, existingDocument)
    }

    private fun productDocument(id: String, name: String, createdAt: Date): DocumentSnapshot {
        val document = mock(DocumentSnapshot::class.java)
        `when`(document.id).thenReturn(id)
        `when`(document.getString("cod")).thenReturn("A-1")
        `when`(document.getString("name")).thenReturn(name)
        `when`(document.getLong("cost")).thenReturn(1234)
        `when`(document.getString("description")).thenReturn("Descricao")
        `when`(document.getTimestamp("createdAt")).thenReturn(
            com.google.firebase.Timestamp(createdAt)
        )
        return document
    }

    private data class FirebaseObjects(
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val products: CollectionReference,
        val newDocument: DocumentReference,
        val existingDocument: DocumentReference
    )
}
