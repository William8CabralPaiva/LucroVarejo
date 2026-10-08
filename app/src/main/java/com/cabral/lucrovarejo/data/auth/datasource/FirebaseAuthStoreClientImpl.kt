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
        firestore.collection("stores")
            .document(uid)
            .set(
                mapOf(
                    "storeName" to profile.storeName,
                    "email" to profile.contactEmail,
                    "createdAt" to FieldValue.serverTimestamp()
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
}
