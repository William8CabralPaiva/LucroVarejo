package com.cabral.lucrovarejo.data.auth.datasource

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.cabral.lucrovarejo.data.store.ActiveStoreKeyProvider
import com.cabral.lucrovarejo.data.store.StoreFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthStoreClientImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val activeStore: ActiveStoreKeyProvider
) : FirebaseAuthStoreClient {

    override val hasSession: Boolean
        get() = auth.currentUser != null

    override val storeKey: String?
        get() = activeStore.currentStoreKey()

    override suspend fun createUser(email: String, password: String): String =
        auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: error("Firebase Auth did not return a user.")

    // A primeira loja da conta usa o UID como id.
    override suspend fun saveStoreProfile(uid: String, profile: StoreProfile) {
        StoreFirestore.newStoreBatch(
            firestore = firestore,
            storeId = uid,
            uid = uid,
            email = profile.contactEmail,
            displayName = profile.displayName
        ).commit().await()
    }

    override suspend fun deleteCurrentUser() {
        auth.currentUser?.delete()?.await()
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun fetchDisplayName(): String? {
        val storeKey = activeStore.currentStoreKey() ?: return null
        return firestore.collection(FirestoreCollections.STORES)
            .document(storeKey)
            .get()
            .await()
            .getString(FIELD_DISPLAY_NAME)
    }

    override suspend fun updateDisplayName(displayName: String) {
        val uid = auth.currentUser?.uid ?: error("No active session.")
        val storeKey = activeStore.currentStoreKey() ?: error("No active session.")
        firestore.collection(FirestoreCollections.STORES)
            .document(storeKey)
            .update(FIELD_DISPLAY_NAME, displayName)
            .await()
        // Copia do nome usada na lista de lojas; se falhar, a loja continua com o nome atualizado.
        try {
            StoreFirestore.userStores(firestore, uid)
                .document(storeKey)
                .update(FIELD_DISPLAY_NAME, displayName)
                .await()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
        }
    }

    override suspend fun requestPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val FIELD_DISPLAY_NAME = "displayName"
    }
}