package com.cabral.lucrovarejo.domain.auth.usecase

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import javax.inject.Inject
import java.util.Locale

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(storeName: String, password: String) {
        repository.signIn(storeName.trim().lowercase(Locale.ROOT), password)
    }
}
