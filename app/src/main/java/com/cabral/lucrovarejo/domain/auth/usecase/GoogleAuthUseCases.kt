package com.cabral.lucrovarejo.domain.auth.usecase

import com.cabral.lucrovarejo.domain.auth.GoogleAuthRepository
import com.cabral.lucrovarejo.domain.auth.GoogleSignInResult
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(idToken: String, displayName: String? = null): GoogleSignInResult =
        repository.signIn(idToken, displayName?.trim())
}

class CompleteGoogleRegistrationUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    suspend operator fun invoke(displayName: String) {
        repository.completeRegistration(displayName.trim())
    }
}

class CancelGoogleRegistrationUseCase @Inject constructor(
    private val repository: GoogleAuthRepository
) {
    operator fun invoke() {
        repository.cancelPendingRegistration()
    }
}
