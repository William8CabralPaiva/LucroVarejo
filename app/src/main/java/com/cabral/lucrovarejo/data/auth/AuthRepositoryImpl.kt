package com.cabral.lucrovarejo.data.auth

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import com.cabral.lucrovarejo.domain.auth.AuthFailure
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
        val value = identifier.trim()
        val email = if (value.contains('@')) {
            value
        } else {
            val storeName = value.lowercase(Locale.ROOT)
            // Contas novas têm e-mail real no Auth; sem registro, é conta antiga com e-mail técnico.
            val registered = try {
                remoteDataSource.findEmailByStoreName(storeName)
            } catch (exception: AuthFailure) {
                null
            }
            registered ?: "$storeName@$LEGACY_AUTH_DOMAIN"
        }
        remoteDataSource.signIn(email = email, password = password)
    }

    override suspend fun requestPasswordReset(identifier: String) {
        val value = identifier.trim()
        val email = if (value.contains('@')) {
            value
        } else {
            remoteDataSource.findEmailByStoreName(value.lowercase(Locale.ROOT))
        }
        // Usuário inexistente também conclui sem erro, para não revelar quais lojas existem.
        if (email != null) remoteDataSource.requestPasswordReset(email)
    }

    override fun signOut() {
        remoteDataSource.signOut()
    }

    private companion object {
        const val LEGACY_AUTH_DOMAIN = "auth.lucrovarejo.invalid"
    }
}
