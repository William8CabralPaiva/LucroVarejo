package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class FakeAuthRemoteDataSource @Inject constructor() : AuthRemoteDataSource {
    override var hasSession: Boolean = false
    override var storeKey: String? = null
    var savedTechnicalEmail: String? = null
    var savedPassword: String? = null
    var savedProfile: StoreProfile? = null
    var signedInTechnicalEmail: String? = null
    var resetEmail: String? = null
    val emailsByStore = mutableMapOf<String, String>()

    override suspend fun createAccount(
        email: String,
        password: String,
        profile: StoreProfile
    ) {
        savedTechnicalEmail = email
        savedPassword = password
        savedProfile = profile
    }

    override suspend fun signIn(email: String, password: String) {
        signedInTechnicalEmail = email
        savedPassword = password
    }

    override suspend fun fetchDisplayName(): String? = savedProfile?.displayName

    override suspend fun findEmailByStoreName(storeName: String): String? =
        emailsByStore[storeName]

    override suspend fun requestPasswordReset(email: String) {
        resetEmail = email
    }

    override fun signOut() {
        hasSession = false
    }
}
