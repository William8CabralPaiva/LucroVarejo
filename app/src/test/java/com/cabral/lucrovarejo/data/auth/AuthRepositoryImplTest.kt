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
    fun registrationUsesRealEmailAsAuthAddressAndSavesProfile() = runTest {
        // Given: a repository connected to a fake remote data source.
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        // When: registration is requested with padded mixed-case values.
        repository.registerStore(" My-Store.1 ", " owner@example.com ", "secret1", " Minha Loja ")

        // Then: identifiers and profile fields are normalized before reaching Firebase.
        assertEquals("owner@example.com", remote.createdEmail)
        assertEquals("secret1", remote.createdPassword)
        assertEquals(
            StoreProfile("my-store.1", "owner@example.com", "Minha Loja"),
            remote.createdProfile
        )
    }

    @Test
    fun blankStoredDisplayNameIsTreatedAsMissing() = runTest {
        val remote = FakeAuthRemoteDataSource().apply { displayName = "  " }
        val repository = AuthRepositoryImpl(remote)

        assertEquals(null, repository.getStoreDisplayName())

        remote.displayName = " Minha Loja "
        assertEquals("Minha Loja", repository.getStoreDisplayName())
    }

    @Test
    fun loginTrimsEmail() = runTest {
        // Given: an auth repository backed by a fake remote source.
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        // When: signing in with a mixed-case store name.
        repository.signIn(" owner@example.com ", "secret1")

        // Then: the same canonical technical address is used.
        assertEquals("owner@example.com", remote.signedInEmail)
        assertEquals("secret1", remote.signedInPassword)
        assertEquals("firebase-uid-123", repository.storeKey)
    }

    @Test
    fun loginWithUsernameUsesLegacyAuthAddress() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        repository.signIn(" My-Store.1 ", "secret1")

        assertEquals("my-store.1@auth.lucrovarejo.invalid", remote.signedInEmail)
    }

    @Test
    fun loginWithEmailKeepsEmailAsAuthAddress() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        repository.signIn(" Owner@Example.com ", "secret1")

        assertEquals("Owner@Example.com", remote.signedInEmail)
    }

    @Test
    fun passwordResetTrimsEmail() = runTest {
        val remote = FakeAuthRemoteDataSource()
        val repository = AuthRepositoryImpl(remote)

        repository.requestPasswordReset(" owner@example.com ")

        assertEquals("owner@example.com", remote.resetEmail)
    }

    @Test
    fun delegatesSessionAndSignOut() {
        // Given: a remote source with an active session.
        val remote = FakeAuthRemoteDataSource().apply {
            hasSession = true
            storeKey = "firebase-uid-123"
        }
        val repository = AuthRepositoryImpl(remote)

        // When: the session is read and sign-out is called.
        assertTrue(repository.hasSession)
        assertEquals("firebase-uid-123", repository.storeKey)
        repository.signOut()
        assertTrue(remote.signedOut)
        assertEquals(null, repository.storeKey)

        remote.hasSession = false
        // Then: session state reflects the remote source after sign-out.
        assertFalse(repository.hasSession)
    }

    private class FakeAuthRemoteDataSource : AuthRemoteDataSource {
        override var hasSession: Boolean = false
        override var storeKey: String? = null
        var createdEmail: String? = null
        var createdPassword: String? = null
        var createdProfile: StoreProfile? = null
        var signedInEmail: String? = null
        var signedInPassword: String? = null
        var signedOut = false
        var displayName: String? = null
        var resetEmail: String? = null

        override suspend fun fetchDisplayName(): String? = displayName

        override suspend fun createAccount(
            email: String,
            password: String,
            profile: StoreProfile
        ) {
            createdEmail = email
            createdPassword = password
            createdProfile = profile
        }

        override suspend fun signIn(email: String, password: String) {
            signedInEmail = email
            signedInPassword = password
            storeKey = "firebase-uid-123"
        }

        override suspend fun requestPasswordReset(email: String) {
            resetEmail = email
        }

        override fun signOut() {
            signedOut = true
            storeKey = null
        }
    }
}
