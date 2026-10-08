package com.cabral.lucrovarejo.data.auth

import com.cabral.lucrovarejo.data.auth.datasource.AuthRemoteDataSource
import com.cabral.lucrovarejo.data.auth.datasource.StoreProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {
    @Test
    fun registrationMapsStoreCredentialsToTechnicalAuthAddressAndProfile() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        repository.registerStore(" My-Store.1 ", " owner@example.com ", "secret1")

        assertEquals("my-store.1@auth.lucrovarejo.invalid", remote.createdEmail)
        assertEquals("secret1", remote.createdPassword)
        assertEquals(StoreProfile("my-store.1", "owner@example.com"), remote.createdProfile)
    }

    @Test
    fun loginUsesTheSameNormalizedTechnicalAddress() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        repository.signIn(" My-Store.1 ", "secret1")

        assertEquals("my-store.1@auth.lucrovarejo.invalid", remote.signedInEmail)
        assertEquals("secret1", remote.signedInPassword)
    }

    @Test
    fun delegatesSessionAndSignOut() {
        val remote = FakeAuthRemoteDataSource().apply { hasSession = true }
        val repository = AuthRepositoryImpl(remote)

        assertTrue(repository.hasSession)
        repository.signOut()
        assertTrue(remote.signedOut)

        remote.hasSession = false
        assertFalse(repository.hasSession)
    }

    private class FakeAuthRemoteDataSource : AuthRemoteDataSource {
        override var hasSession: Boolean = false
        var createdEmail: String? = null
        var createdPassword: String? = null
        var createdProfile: StoreProfile? = null
        var signedInEmail: String? = null
        var signedInPassword: String? = null
        var signedOut = false

        override suspend fun createAccount(
            technicalEmail: String,
            password: String,
            profile: StoreProfile
        ) {
            createdEmail = technicalEmail
            createdPassword = password
            createdProfile = profile
        }

        override suspend fun signIn(technicalEmail: String, password: String) {
            signedInEmail = technicalEmail
            signedInPassword = password
        }

        override fun signOut() {
            signedOut = true
        }
    }
}
