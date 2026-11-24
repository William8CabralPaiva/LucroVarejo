package com.cabral.lucrovarejo.data.store

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoreDataTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val auth = mock(FirebaseAuth::class.java)
    private val user = mock(FirebaseUser::class.java)
    private val firestore = mock(FirebaseFirestore::class.java)
    private val users = mock(CollectionReference::class.java)
    private val userDoc = mock(DocumentReference::class.java)
    private val userStores = mock(CollectionReference::class.java)
    private val stores = mock(CollectionReference::class.java)
    private val batch = mock(WriteBatch::class.java)

    init {
        `when`(user.uid).thenReturn("uid-1")
        `when`(user.email).thenReturn("a@b.c")
        `when`(auth.currentUser).thenReturn(user)
        `when`(firestore.collection(FirestoreCollections.USERS)).thenReturn(users)
        `when`(users.document("uid-1")).thenReturn(userDoc)
        `when`(userDoc.collection(FirestoreCollections.USER_STORES)).thenReturn(userStores)
        `when`(firestore.collection(FirestoreCollections.STORES)).thenReturn(stores)
    }

    private fun holder() = ActiveStoreHolder(auth, context)

    private fun summary(id: String, name: String?): DocumentSnapshot {
        val doc = mock(DocumentSnapshot::class.java)
        `when`(doc.id).thenReturn(id)
        `when`(doc.getString("displayName")).thenReturn(name)
        return doc
    }

    private fun stubUserStores(vararg docs: DocumentSnapshot) {
        val snapshot = mock(QuerySnapshot::class.java)
        `when`(snapshot.documents).thenReturn(docs.toList())
        `when`(userStores.get()).thenReturn(Tasks.forResult(snapshot))
    }

    @Test
    fun holderFallsBackToUidThenRemembersSelection() {
        val holder = holder()
        assertEquals("uid-1", holder.currentStoreKey())

        holder.select("store-9")
        assertEquals("store-9", holder.currentStoreKey())
        assertEquals("store-9", holder().currentStoreKey())
    }

    @Test
    fun holderWithoutSessionHasNoStore() {
        `when`(auth.currentUser).thenReturn(null)
        val holder = holder()

        assertNull(holder.currentStoreKey())
        holder.select("x")
        assertNull(holder.currentStoreKey())
    }

    @Test
    fun holderCacheFollowsUserChange() {
        val holder = holder()
        holder.select("store-9")
        val other = mock(FirebaseUser::class.java)
        `when`(other.uid).thenReturn("uid-2")
        `when`(auth.currentUser).thenReturn(other)

        assertEquals("uid-2", holder.currentStoreKey())
    }

    @Test
    fun selectionListsStoresSortedAndKeepsLegacyOutOfWhenAlreadyListed() = runTest {
        stubUserStores(summary("uid-1", "zeta"), summary("b", "Alfa"), summary("c", null))
        val repository = StoreRepositoryImpl(auth, firestore, holder())

        val selection = repository.getStoreSelection()

        assertEquals(listOf("c", "b", "uid-1"), selection.stores.map { it.id })
        assertEquals("", selection.stores.first().displayName)
        assertEquals("uid-1", selection.activeStoreId)
        verify(stores, never()).document("uid-1")
    }

    private fun stubLegacy(exists: Boolean, name: String? = "Antiga") {
        val doc = mock(DocumentReference::class.java)
        val snapshot = mock(DocumentSnapshot::class.java)
        `when`(snapshot.exists()).thenReturn(exists)
        `when`(snapshot.getString("displayName")).thenReturn(name)
        `when`(doc.get()).thenReturn(Tasks.forResult(snapshot))
        `when`(stores.document("uid-1")).thenReturn(doc)
    }

    @Test
    fun selectionAddsAndBackfillsLegacyStore() = runTest {
        stubUserStores()
        stubLegacy(exists = true)
        val shortcut = mock(DocumentReference::class.java)
        `when`(userStores.document("uid-1")).thenReturn(shortcut)
        `when`(shortcut.set(any())).thenReturn(Tasks.forResult(null))

        val selection = StoreRepositoryImpl(auth, firestore, holder()).getStoreSelection()

        assertEquals(listOf(StoreSummary("uid-1", "Antiga")), selection.stores)
        verify(shortcut).set(any())
    }

    @Test
    fun selectionToleratesBackfillFailureAndMissingLegacy() = runTest {
        stubUserStores()
        stubLegacy(exists = true)
        val shortcut = mock(DocumentReference::class.java)
        `when`(userStores.document("uid-1")).thenReturn(shortcut)
        `when`(shortcut.set(any())).thenReturn(Tasks.forException(RuntimeException("boom")))
        val repository = StoreRepositoryImpl(auth, firestore, holder())

        assertEquals(1, repository.getStoreSelection().stores.size)

        stubLegacy(exists = false)
        assertTrue(repository.getStoreSelection().stores.isEmpty())
    }

    @Test
    fun selectionWithoutSessionFails() = runTest {
        `when`(auth.currentUser).thenReturn(null)
        try {
            StoreRepositoryImpl(auth, firestore, holder()).getStoreSelection()
            fail()
        } catch (_: IllegalStateException) {
        }
    }

    @Test
    fun createStoreCommitsBatchAndTrimsName() = runTest {
        val newStore = mock(DocumentReference::class.java)
        val members = mock(CollectionReference::class.java)
        val member = mock(DocumentReference::class.java)
        val shortcut = mock(DocumentReference::class.java)
        `when`(stores.document()).thenReturn(newStore)
        `when`(stores.document("new-id")).thenReturn(newStore)
        `when`(newStore.id).thenReturn("new-id")
        `when`(newStore.collection(FirestoreCollections.MEMBERS)).thenReturn(members)
        `when`(members.document("uid-1")).thenReturn(member)
        `when`(userStores.document("new-id")).thenReturn(shortcut)
        `when`(firestore.batch()).thenReturn(batch)
        `when`(batch.set(any(), any())).thenReturn(batch)
        `when`(batch.commit()).thenReturn(Tasks.forResult(null))
        val holder = holder()
        val repository = StoreRepositoryImpl(auth, firestore, holder)

        val created = repository.createStore("  Nova  ")
        repository.selectStore("new-id")

        assertEquals(StoreSummary("new-id", "Nova"), created)
        verify(batch).set(newStore, mapOf(
            "email" to "a@b.c",
            "displayName" to "Nova",
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        ))
        verify(batch).commit()
        assertEquals("new-id", holder.currentStoreKey())
    }

    @Test
    fun createStoreWithoutSessionFails() = runTest {
        `when`(auth.currentUser).thenReturn(null)
        try {
            StoreRepositoryImpl(auth, firestore, holder()).createStore("X")
            fail()
        } catch (_: IllegalStateException) {
        }
    }
}
