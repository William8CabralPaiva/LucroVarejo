package com.cabral.lucrovarejo.domain.auth.usecase

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {
    @Test
    fun registerNormalizesStoreNameAndTrimsEmail() = runTest {
        // Given: a use case backed by a fake auth repository.
        val repository = FakeAuthRepository()

        // When: registering with mixed-case store name and padded email.
        RegisterStoreUseCase(repository)(" My-Store.1 ", " owner@example.com ", "secret1", " Minha Loja ")

        // Then: normalized values are sent to the repository.
        assertEquals("my-store.1", repository.registeredStore)
        assertEquals("owner@example.com", repository.registeredEmail)
        assertEquals("secret1", repository.registeredPassword)
        assertEquals("Minha Loja", repository.registeredDisplayName)
    }

    @Test
    fun signInTrimsEmailButKeepsPassword() = runTest {
        // Given: a sign-in use case backed by a fake repository.
        val repository = FakeAuthRepository()

        // When: signing in with a mixed-case store name.
        SignInUseCase(repository)(" owner@example.com ", "secret1")

        // Then: only the store name is normalized.
        assertEquals("owner@example.com", repository.signedInStore)
        assertEquals("secret1", repository.signedInPassword)
    }

    @Test
    fun requestPasswordResetTrimsEmail() = runTest {
        val repository = FakeAuthRepository()

        RequestPasswordResetUseCase(repository)(" owner@example.com ")

        assertEquals("owner@example.com", repository.resetEmail)
    }

    @Test
    fun sessionAndSignOutUseRepository() {
        // Given: a repository that currently has an active session.
        val repository = FakeAuthRepository().apply { hasSession = true }

        // When: querying the session and signing out.
        assertTrue(HasSessionUseCase(repository)())
        SignOutUseCase(repository)()
        assertTrue(repository.signedOut)

        repository.hasSession = false
        // Then: the session state is false after sign-out.
        assertFalse(HasSessionUseCase(repository)())
    }

    private class FakeAuthRepository : AuthRepository {
        override var hasSession: Boolean = false
        override var storeKey: String? = null
        var registeredStore: String? = null
        var registeredEmail: String? = null
        var registeredPassword: String? = null
        var registeredDisplayName: String? = null
        var signedInStore: String? = null
        var signedInPassword: String? = null
        var signedOut = false
        var resetEmail: String? = null

        override suspend fun registerStore(
            storeName: String,
            email: String,
            password: String,
            displayName: String
        ) {
            registeredStore = storeName
            registeredEmail = email
            registeredPassword = password
            registeredDisplayName = displayName
        }

        override suspend fun getStoreDisplayName(): String? = null

        override suspend fun signIn(identifier: String, password: String) {
            signedInStore = identifier
            signedInPassword = password
        }

        override suspend fun requestPasswordReset(email: String) {
            resetEmail = email
        }

        override fun signOut() {
            signedOut = true
            hasSession = false
        }
    }
}
