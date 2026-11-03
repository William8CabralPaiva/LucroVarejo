package com.cabral.lucrovarejo.data.auth

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import com.cabral.lucrovarejo.domain.auth.AuthRepository
import javax.inject.Singleton
import javax.inject.Inject
import java.util.Locale

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {
    override val hasSession: Boolean
        get() = remoteDataSource.hasSession

    override val storeKey: String?
        get() = remoteDataSource.storeKey

    override suspend fun registerStore(
        storeName: String,
        email: String,
        password: String,
        displayName: String
    ) {
        val normalizedEmail = email.trim()
        remoteDataSource.createAccount(
            email = normalizedEmail,
            password = password,
            profile = StoreProfile(
                storeName = storeName.trim().lowercase(Locale.ROOT),
                contactEmail = normalizedEmail,
                displayName = displayName.trim()
            )
        )
    }

    override suspend fun getStoreDisplayName(): String? =
        remoteDataSource.fetchDisplayName()?.trim()?.takeIf { it.isNotEmpty() }

    override suspend fun signIn(identifier: String, password: String) {
        remoteDataSource.signIn(email = authEmail(identifier), password = password)
    }

    override suspend fun requestPasswordReset(email: String) {
        remoteDataSource.requestPasswordReset(email.trim())
    }

    override fun signOut() {
        remoteDataSource.signOut()
    }

    // Contas antigas foram criadas com e-mail técnico derivado do usuário.
    private fun authEmail(identifier: String): String {
        val value = identifier.trim()
        return if (value.contains('@')) value else "${value.lowercase(Locale.ROOT)}@$LEGACY_AUTH_DOMAIN"
    }

    private companion object {
        const val LEGACY_AUTH_DOMAIN = "auth.lucrovarejo.invalid"
    }
}
