package com.cabral.lucrovarejo.data.auth.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseAuthStoreClientImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FirebaseAuthStoreClient {

    override val hasSession: Boolean
        get() = auth.currentUser != null

    override suspend fun createUser(email: String, password: String): String =
        auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: error("Firebase Auth did not return a user.")

    override suspend fun saveStoreProfile(uid: String, profile: StoreProfile) {
        firestore.collection(FIELD_STORES)
            .document(uid)
            .set(
                mapOf(
                    FIELD_STORE_NAME to profile.storeName,
                    FIELD_EMAIL to profile.contactEmail,
                    FIELD_CREATED_AT to FieldValue.serverTimestamp()
                )
            )
            .await()
    }

    override suspend fun deleteCurrentUser() {
        auth.currentUser?.delete()?.await()
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun signOut() {
        auth.signOut()
    }

    private companion object {
        const val FIELD_STORES = "stores"
        const val FIELD_STORE_NAME = "storeName"
        const val FIELD_EMAIL = "email"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
