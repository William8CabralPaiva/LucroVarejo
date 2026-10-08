package com.cabral.lucrovarejo.domain.auth

interface AuthRepository {
    val hasSession: Boolean

    suspend fun registerStore(storeName: String, email: String, password: String)

    suspend fun signIn(storeName: String, password: String)

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
