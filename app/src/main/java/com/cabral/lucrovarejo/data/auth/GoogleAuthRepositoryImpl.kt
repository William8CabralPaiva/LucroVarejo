package com.cabral.lucrovarejo.data.auth

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.cabral.lucrovarejo.data.store.StoreFirestore
import com.cabral.lucrovarejo.domain.auth.GoogleAuthRepository
import com.cabral.lucrovarejo.domain.auth.GoogleSignInResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class GoogleAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : GoogleAuthRepository {

    override suspend fun signIn(idToken: String, displayName: String?): GoogleSignInResult {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.signInWithCredential(credential).await().user
            ?: error("Firebase Auth did not return a user.")
        return try {
            when {
                storeExists(user.uid) -> GoogleSignInResult.Success
                displayName != null -> {
                    createStore(user, displayName)
                    GoogleSignInResult.Success
                }
                else -> GoogleSignInResult.NeedsStoreName
            }
        } catch (exception: Exception) {
            auth.signOut()
            throw exception
        }
    }

    override suspend fun completeRegistration(displayName: String) {
        val user = auth.currentUser ?: error("No active session.")
        createStore(user, displayName)
    }

    override fun cancelPendingRegistration() {
        auth.signOut()
    }

    private suspend fun storeExists(uid: String): Boolean =
        firestore.collection(FirestoreCollections.STORES)
            .document(uid)
            .get()
            .await()
            .exists()

    // A primeira loja da conta usa o UID como id, igual ao cadastro por e-mail.
    private suspend fun createStore(user: FirebaseUser, displayName: String) {
        val email = user.email?.takeIf { it.isNotBlank() } ?: error("Google account without e-mail.")
        StoreFirestore.newStoreBatch(
            firestore = firestore,
            storeId = user.uid,
            uid = user.uid,
            email = email,
            displayName = displayName.trim()
        ).commit().await()
    }
}
