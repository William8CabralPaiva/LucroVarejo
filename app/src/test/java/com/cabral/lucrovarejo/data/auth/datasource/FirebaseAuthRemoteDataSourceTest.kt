package com.cabral.lucrovarejo.data.auth.datasource

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.cabral.lucrovarejo.domain.auth.AuthFailure

class FirebaseAuthRemoteDataSourceTest {
    @Test
    fun createsAccountAndSavesProfileForTheNewUid() = runTest {
        val client = FakeFirebaseAuthStoreClient()
        val dataSource = FirebaseAuthRemoteDataSourceImpl(client)
        val profile = StoreProfile("loja-1", "owner@example.com")

        dataSource.createAccount("loja-1@auth.lucrovarejo.invalid", "secret1", profile)

        assertEquals("loja-1@auth.lucrovarejo.invalid", client.createdEmail)
        assertEquals("secret1", client.createdPassword)
        assertEquals("uid-1", client.savedUid)
        assertEquals(profile, client.savedProfile)
        assertFalse(client.deletedUser)
    }

    @Test
    fun rollsBackAuthAccountWhenProfileWriteFails() = runTest {
        val client = FakeFirebaseAuthStoreClient().apply {
            profileWriteFailure = IllegalStateException("Firestore unavailable")
        }
        val dataSource = FirebaseAuthRemoteDataSourceImpl(client)

        val failure = runCatching {
            dataSource.createAccount(
                "loja-1@auth.lucrovarejo.invalid",
                "secret1",
                StoreProfile("loja-1", "owner@example.com")
            )
        }
            .exceptionOrNull()

        assertTrue(failure is AuthFailure)
        assertTrue(client.deletedUser)
        assertTrue(client.signedOut)
    }

    @Test
    fun delegatesSessionLoginAndSignOut() = runTest {
        val client = FakeFirebaseAuthStoreClient().apply { hasSession = true }
        val dataSource = FirebaseAuthRemoteDataSourceImpl(client)

        assertTrue(dataSource.hasSession)
        dataSource.signIn("loja@auth.lucrovarejo.invalid", "secret1")
        dataSource.signOut()

        assertEquals("loja@auth.lucrovarejo.invalid", client.signedInEmail)
        assertEquals("secret1", client.signedInPassword)
        assertTrue(client.signedOut)
    }

    private class FakeFirebaseAuthStoreClient : FirebaseAuthStoreClient {
        override var hasSession: Boolean = false
        var createdEmail: String? = null
        var createdPassword: String? = null
        var savedUid: String? = null
        var savedProfile: StoreProfile? = null
        var profileWriteFailure: Exception? = null
        var deletedUser = false
        var signedOut = false
        var signedInEmail: String? = null
        var signedInPassword: String? = null

        override suspend fun createUser(email: String, password: String): String {
            createdEmail = email
            createdPassword = password
            return "uid-1"
        }

        override suspend fun saveStoreProfile(uid: String, profile: StoreProfile) {
            profileWriteFailure?.let { throw it }
            savedUid = uid
            savedProfile = profile
        }

        override suspend fun deleteCurrentUser() {
            deletedUser = true
        }

        override suspend fun signIn(email: String, password: String) {
            signedInEmail = email
            signedInPassword = password
        }

        override fun signOut() {
            signedOut = true
        }
    }
}
