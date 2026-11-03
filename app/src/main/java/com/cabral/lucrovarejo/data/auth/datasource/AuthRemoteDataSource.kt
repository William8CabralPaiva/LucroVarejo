package com.cabral.lucrovarejo.data.auth.datasource

data class StoreProfile(
    val storeName: String,
    val contactEmail: String,
    val displayName: String
)

interface AuthRemoteDataSource {
    val hasSession: Boolean
    val storeKey: String?

    suspend fun createAccount(
        email: String,
        password: String,
        profile: StoreProfile
    )

    suspend fun signIn(email: String, password: String)

    suspend fun fetchDisplayName(): String?

    suspend fun requestPasswordReset(email: String)

    fun signOut()
}

interface FirebaseAuthStoreClient {
    val hasSession: Boolean
    val storeKey: String?

    suspend fun createUser(email: String, password: String): String

    suspend fun saveStoreProfile(uid: String, profile: StoreProfile)

    suspend fun deleteCurrentUser()

    suspend fun signIn(email: String, password: String)

    suspend fun fetchDisplayName(): String?

    suspend fun requestPasswordReset(email: String)

    fun signOut()
}
