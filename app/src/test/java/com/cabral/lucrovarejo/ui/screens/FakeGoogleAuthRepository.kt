package com.cabral.lucrovarejo.ui.screens

import com.cabral.lucrovarejo.domain.auth.GoogleAuthRepository
import com.cabral.lucrovarejo.domain.auth.GoogleSignInResult

class FakeGoogleAuthRepository : GoogleAuthRepository {
    var storeExists = false
    var failure = false
    val signInCalls = mutableListOf<Pair<String, String?>>()
    val createdStores = mutableListOf<String>()
    var cancelCount = 0

    override suspend fun signIn(idToken: String, displayName: String?): GoogleSignInResult {
        if (failure) error("boom")
        signInCalls += idToken to displayName
        return when {
            storeExists -> GoogleSignInResult.Success
            displayName != null -> {
                createdStores += displayName
                GoogleSignInResult.Success
            }
            else -> GoogleSignInResult.NeedsStoreName
        }
    }

    override suspend fun completeRegistration(displayName: String) {
        if (failure) error("boom")
        createdStores += displayName
    }

    override fun cancelPendingRegistration() {
        cancelCount++
    }
}
