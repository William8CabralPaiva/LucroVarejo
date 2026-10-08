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

    override suspend fun registerStore(storeName: String, email: String, password: String) {
        val normalizedStoreName = storeName.trim().lowercase(Locale.ROOT)
        remoteDataSource.createAccount(
            technicalEmail = technicalEmail(normalizedStoreName),
            password = password,
            profile = StoreProfile(
                storeName = normalizedStoreName,
                contactEmail = email.trim()
            )
        )
    }

    override suspend fun signIn(storeName: String, password: String) {
        remoteDataSource.signIn(
            technicalEmail = technicalEmail(storeName.trim().lowercase(Locale.ROOT)),
            password = password
        )
    }

    override fun signOut() {
        remoteDataSource.signOut()
    }

    private fun technicalEmail(storeName: String) =
        "$storeName@auth.lucrovarejo.invalid"
}
