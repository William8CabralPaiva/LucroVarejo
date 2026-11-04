package com.cabral.lucrovarejo.domain.auth.usecase

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) {
        repository.signIn(email.trim(), password)
    }
}