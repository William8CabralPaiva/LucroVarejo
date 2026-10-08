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
        val repository = FakeAuthRepository()

        RegisterStoreUseCase(repository)(" My-Store.1 ", " owner@example.com ", "secret1")

        assertEquals("my-store.1", repository.registeredStore)
        assertEquals("owner@example.com", repository.registeredEmail)
        assertEquals("secret1", repository.registeredPassword)
    }

    @Test
    fun signInNormalizesStoreNameButKeepsPassword() = runTest {
        val repository = FakeAuthRepository()

        SignInUseCase(repository)(" My-Store.1 ", "secret1")

        assertEquals("my-store.1", repository.signedInStore)
        assertEquals("secret1", repository.signedInPassword)
    }

    @Test
    fun sessionAndSignOutUseRepository() {
        val repository = FakeAuthRepository().apply { hasSession = true }

        assertTrue(HasSessionUseCase(repository)())
        SignOutUseCase(repository)()
        assertTrue(repository.signedOut)

        repository.hasSession = false
        assertFalse(HasSessionUseCase(repository)())
    }

    private class FakeAuthRepository : AuthRepository {
        override var hasSession: Boolean = false
        var registeredStore: String? = null
        var registeredEmail: String? = null
        var registeredPassword: String? = null
        var signedInStore: String? = null
        var signedInPassword: String? = null
        var signedOut = false

        override suspend fun registerStore(storeName: String, email: String, password: String) {
            registeredStore = storeName
            registeredEmail = email
            registeredPassword = password
        }

        override suspend fun signIn(storeName: String, password: String) {
            signedInStore = storeName
            signedInPassword = password
        }

        override fun signOut() {
            signedOut = true
            hasSession = false
        }
    }
}
