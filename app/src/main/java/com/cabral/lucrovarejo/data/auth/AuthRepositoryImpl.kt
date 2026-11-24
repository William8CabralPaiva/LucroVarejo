package com.cabral.lucrovarejo.data.auth

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {
    override val hasSession: Boolean
        get() = remoteDataSource.hasSession

    override val storeKey: String?
        get() = remoteDataSource.storeKey

    override suspend fun registerStore(
        email: String,
        password: String,
        displayName: String
    ) {
        val normalizedEmail = email.trim()
        remoteDataSource.createAccount(
            email = normalizedEmail,
            password = password,
            profile = StoreProfile(
                contactEmail = normalizedEmail,
                displayName = displayName.trim()
            )
        )
    }

    override suspend fun getStoreDisplayName(): String? =
        remoteDataSource.fetchDisplayName()?.trim()?.takeIf { it.isNotEmpty() }

    override suspend fun updateStoreDisplayName(displayName: String) {
        remoteDataSource.updateDisplayName(displayName.trim())
    }

    override suspend fun signIn(email: String, password: String) {
        remoteDataSource.signIn(email = email.trim(), password = password)
    }

    override suspend fun requestPasswordReset(email: String) {
        remoteDataSource.requestPasswordReset(email.trim())
    }

    override fun signOut() {
        remoteDataSource.signOut()
    }
}