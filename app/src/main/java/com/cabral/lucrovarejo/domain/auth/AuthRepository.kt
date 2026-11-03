package com.cabral.lucrovarejo.domain.auth

interface AuthRepository {
    val hasSession: Boolean
    val storeKey: String?

    suspend fun registerStore(
        storeName: String,
        email: String,
        password: String,
        displayName: String
    )

    suspend fun signIn(identifier: String, password: String)

    suspend fun getStoreDisplayName(): String?

    suspend fun requestPasswordReset(email: String)

    fun signOut()
}

class StoreAlreadyRegisteredException : Exception()

enum class AuthFailureReason {
    PROVIDER_DISABLED,
    INVALID_ACCOUNT_IDENTIFIER,
    PERMISSION_DENIED,
    NETWORK,
    UNKNOWN
}

class AuthFailure(
    val reason: AuthFailureReason,
    cause: Throwable? = null
) : Exception(cause)
