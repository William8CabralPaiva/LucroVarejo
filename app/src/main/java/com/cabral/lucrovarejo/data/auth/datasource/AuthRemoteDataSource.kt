package com.cabral.lucrovarejo.data.auth.datasource

data class StoreProfile(
    val storeName: String,
    val contactEmail: String
)

interface AuthRemoteDataSource {
    val hasSession: Boolean

    suspend fun createAccount(
        technicalEmail: String,
        password: String,
        profile: StoreProfile
    )

    suspend fun signIn(technicalEmail: String, password: String)

    fun signOut()
}

interface FirebaseAuthStoreClient {
    val hasSession: Boolean

    suspend fun createUser(email: String, password: String): String

    suspend fun saveStoreProfile(uid: String, profile: StoreProfile)

    suspend fun deleteCurrentUser()

    suspend fun signIn(email: String, password: String)

    fun signOut()
}
