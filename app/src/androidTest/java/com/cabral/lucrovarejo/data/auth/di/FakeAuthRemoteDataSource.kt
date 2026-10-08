package com.cabral.lucrovarejo.data.auth.di

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import javax.inject.Singleton
import javax.inject.Inject

@Singleton
class FakeAuthRemoteDataSource @Inject constructor() : AuthRemoteDataSource {
    override var hasSession: Boolean = false
    var savedTechnicalEmail: String? = null
    var savedPassword: String? = null
    var savedProfile: StoreProfile? = null
    var signedInTechnicalEmail: String? = null

    override suspend fun createAccount(
        technicalEmail: String,
        password: String,
        profile: StoreProfile
    ) {
        savedTechnicalEmail = technicalEmail
        savedPassword = password
        savedProfile = profile
    }

    override suspend fun signIn(technicalEmail: String, password: String) {
        signedInTechnicalEmail = technicalEmail
        savedPassword = password
    }

    override fun signOut() {
        hasSession = false
    }
}
