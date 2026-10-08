package com.cabral.lucrovarejo.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {
    override val hasSession: Boolean
        get() = auth.currentUser != null

    override suspend fun register(storeName: String, email: String, password: String) {
        val normalizedStoreName = storeName.lowercase(Locale.ROOT)
        val technicalEmail = technicalEmail(normalizedStoreName)
        val user = try {
            auth.createUserWithEmailAndPassword(technicalEmail, password).await().user
                ?: error("Firebase Auth did not return a user.")
        } catch (exception: FirebaseAuthUserCollisionException) {
            throw StoreAlreadyRegisteredException()
        }

        try {
            firestore.collection("stores")
                .document(user.uid)
                .set(
                    mapOf(
                        "storeName" to normalizedStoreName,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        } catch (profileFailure: Exception) {
            try {
                user.delete().await()
            } catch (rollbackFailure: Exception) {
                profileFailure.addSuppressed(rollbackFailure)
            } finally {
                auth.signOut()
            }
            throw profileFailure
        }
    }

    override suspend fun signIn(storeName: String, password: String) {
        auth.signInWithEmailAndPassword(
            technicalEmail(storeName.lowercase(Locale.ROOT)),
            password
        ).await()
    }

    override fun signOut() {
        auth.signOut()
    }

    private fun technicalEmail(storeName: String): String =
        "$storeName@auth.lucrovarejo.invalid"
}
