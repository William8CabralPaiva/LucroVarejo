package com.cabral.lucrovarejo.data.auth

interface AuthRepository {
    val hasSession: Boolean

    suspend fun register(storeName: String, email: String, password: String)

    suspend fun signIn(storeName: String, password: String)

    fun signOut()
}

class StoreAlreadyRegisteredException : Exception()
