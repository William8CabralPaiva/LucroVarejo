package com.cabral.lucrovarejo.data.auth.datasource

import com.cabral.lucrovarejo.domain.auth.StoreAlreadyRegisteredException
import com.cabral.lucrovarejo.domain.auth.AuthFailure
import com.cabral.lucrovarejo.domain.auth.AuthFailureReason
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import javax.inject.Inject

class FirebaseAuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseClient: FirebaseAuthStoreClient
) : AuthRemoteDataSource {
    override val hasSession: Boolean
        get() = firebaseClient.hasSession

    override suspend fun createAccount(
        technicalEmail: String,
        password: String,
        profile: StoreProfile
    ) {
        val uid = try {
            firebaseClient.createUser(technicalEmail, password)
        } catch (exception: FirebaseAuthUserCollisionException) {
            throw StoreAlreadyRegisteredException()
        } catch (exception: Exception) {
            throw mapFailure(exception)
        }
        try {
            firebaseClient.saveStoreProfile(uid = uid, profile = profile)
        } catch (profileFailure: Exception) {
            try {
                firebaseClient.deleteCurrentUser()
            } catch (rollbackFailure: Exception) {
                profileFailure.addSuppressed(rollbackFailure)
            } finally {
                firebaseClient.signOut()
            }
            throw mapFailure(profileFailure)
        }
    }

    override suspend fun signIn(technicalEmail: String, password: String) {
        try {
            firebaseClient.signIn(technicalEmail, password)
        } catch (exception: Exception) {
            throw mapFailure(exception)
        }
    }

    override fun signOut() {
        firebaseClient.signOut()
    }

    private fun mapFailure(exception: Exception): Exception {
        if (exception is FirebaseAuthUserCollisionException) {
            return StoreAlreadyRegisteredException()
        }

        val reason = when {
            exception is FirebaseAuthException &&
                exception.errorCode == "ERROR_OPERATION_NOT_ALLOWED" ->
                AuthFailureReason.PROVIDER_DISABLED
            exception is FirebaseAuthException &&
                exception.errorCode == "ERROR_INVALID_EMAIL" ->
                AuthFailureReason.INVALID_ACCOUNT_IDENTIFIER
            exception is FirebaseAuthException &&
                exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE" ->
                AuthFailureReason.UNKNOWN
            exception is FirebaseFirestoreException &&
                exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                AuthFailureReason.PERMISSION_DENIED
            exception is FirebaseNetworkException ||
                exception is FirebaseFirestoreException &&
                exception.code == FirebaseFirestoreException.Code.UNAVAILABLE ->
                AuthFailureReason.NETWORK
            else -> AuthFailureReason.UNKNOWN
        }
        return AuthFailure(reason, exception)
    }
}
