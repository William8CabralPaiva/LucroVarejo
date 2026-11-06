package com.cabral.lucrovarejo.data.auth.datasource

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FirebaseAuthStoreClientImplTest {
    private val auth = mock(FirebaseAuth::class.java)
    private val firestore = mock(FirebaseFirestore::class.java)
    private val user = mock(FirebaseUser::class.java)
    private val stores = mock(CollectionReference::class.java)
    private val store = mock(DocumentReference::class.java)
    private val users = mock(CollectionReference::class.java)
    private val userDoc = mock(DocumentReference::class.java)
    private val userStores = mock(CollectionReference::class.java)
    private val userStore = mock(DocumentReference::class.java)
    private val members = mock(CollectionReference::class.java)
    private val member = mock(DocumentReference::class.java)
    private val batch = mock(WriteBatch::class.java)
    private var storeKey: String? = "store-1"

    init {
        `when`(user.uid).thenReturn("uid-1")
        `when`(firestore.collection(FirestoreCollections.STORES)).thenReturn(stores)
        `when`(stores.document(any<String>())).thenReturn(store)
        `when`(store.collection(FirestoreCollections.MEMBERS)).thenReturn(members)
        `when`(members.document(any<String>())).thenReturn(member)
        `when`(firestore.collection(FirestoreCollections.USERS)).thenReturn(users)
        `when`(users.document(any<String>())).thenReturn(userDoc)
        `when`(userDoc.collection(FirestoreCollections.USER_STORES)).thenReturn(userStores)
        `when`(userStores.document(any<String>())).thenReturn(userStore)
        `when`(firestore.batch()).thenReturn(batch)
        `when`(batch.set(any(), any())).thenReturn(batch)
        `when`(batch.commit()).thenReturn(Tasks.forResult(null))
    }

    private val client get() = FirebaseAuthStoreClientImpl(auth, firestore) { storeKey }

    @Test
    fun sessionAndStoreKeyReflectAuthAndProvider() {
        `when`(auth.currentUser).thenReturn(null)
        assertFalse(client.hasSession)
        storeKey = null
        assertNull(client.storeKey)

        `when`(auth.currentUser).thenReturn(user)
        storeKey = "k"
        assertTrue(client.hasSession)
        assertEquals("k", client.storeKey)
    }

    @Test
    fun createUserReturnsUidOrFailsWithoutUser() = runTest {
        val result = mock(AuthResult::class.java)
        `when`(result.user).thenReturn(user)
        `when`(auth.createUserWithEmailAndPassword("a@b.com", "123456")).thenReturn(Tasks.forResult(result))
        assertEquals("uid-1", client.createUser("a@b.com", "123456"))

        val empty = mock(AuthResult::class.java)
        `when`(auth.createUserWithEmailAndPassword("x@b.com", "123456")).thenReturn(Tasks.forResult(empty))
        try {
            client.createUser("x@b.com", "123456")
            fail()
        } catch (_: IllegalStateException) {
        }
    }

    @Test
    fun saveStoreProfileCommitsBatchWithThreeWrites() = runTest {
        client.saveStoreProfile("uid-1", StoreProfile("a@b.com", "Loja"))

        verify(batch).set(eq(store), any())
        verify(batch).set(eq(member), any())
        verify(batch).set(eq(userStore), any())
        verify(batch).commit()
    }

    @Test
    fun signInResetSignOutAndDeleteDelegateToAuth() = runTest {
        `when`(auth.signInWithEmailAndPassword("a", "b")).thenReturn(Tasks.forResult(mock(AuthResult::class.java)))
        `when`(auth.sendPasswordResetEmail("a")).thenReturn(Tasks.forResult(null))
        `when`(user.delete()).thenReturn(Tasks.forResult(null))

        client.signIn("a", "b")
        client.requestPasswordReset("a")
        client.signOut()
        verify(auth).signInWithEmailAndPassword("a", "b")
        verify(auth).sendPasswordResetEmail("a")
        verify(auth).signOut()

        `when`(auth.currentUser).thenReturn(null)
        client.deleteCurrentUser()
        verify(user, never()).delete()

        `when`(auth.currentUser).thenReturn(user)
        client.deleteCurrentUser()
        verify(user).delete()
    }

    @Test
    fun fetchDisplayNameReadsStoreDocumentOrNullWithoutStore() = runTest {
        val snapshot = mock(DocumentSnapshot::class.java)
        `when`(snapshot.getString("displayName")).thenReturn("Loja")
        `when`(store.get()).thenReturn(Tasks.forResult(snapshot))

        assertEquals("Loja", client.fetchDisplayName())

        storeKey = null
        assertNull(client.fetchDisplayName())
    }

    @Test
    fun updateDisplayNameUpdatesStoreAndUserShortcut() = runTest {
        `when`(auth.currentUser).thenReturn(user)
        `when`(store.update("displayName", "Nova")).thenReturn(Tasks.forResult(null))
        `when`(userStore.update("displayName", "Nova")).thenReturn(Tasks.forResult(null))

        client.updateDisplayName("Nova")

        verify(store).update("displayName", "Nova")
        verify(userStore).update("displayName", "Nova")
    }

    @Test
    fun updateDisplayNameIgnoresShortcutFailure() = runTest {
        `when`(auth.currentUser).thenReturn(user)
        `when`(store.update("displayName", "Nova")).thenReturn(Tasks.forResult(null))
        `when`(userStore.update("displayName", "Nova"))
            .thenReturn(Tasks.forException(RuntimeException("offline")))

        client.updateDisplayName("Nova")

        verify(store).update("displayName", "Nova")
    }

    @Test
    fun updateDisplayNameRequiresSessionAndStore() = runTest {
        `when`(auth.currentUser).thenReturn(null)
        try {
            client.updateDisplayName("Nova")
            fail()
        } catch (_: IllegalStateException) {
        }

        `when`(auth.currentUser).thenReturn(user)
        storeKey = null
        try {
            client.updateDisplayName("Nova")
            fail()
        } catch (_: IllegalStateException) {
        }
    }
}
