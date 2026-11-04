package com.cabral.lucrovarejo.domain.auth.usecase

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import javax.inject.Inject

class RegisterStoreUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ) {
        repository.registerStore(
            email = email.trim(),
            password = password,
            displayName = displayName.trim()
        )
    }
}