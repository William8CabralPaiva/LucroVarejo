package com.cabral.lucrovarejo.domain.auth.usecase

import com.cabral.lucrovarejo.domain.auth.AuthRepository
import javax.inject.Inject
import java.util.Locale

class RegisterStoreUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(storeName: String, email: String, password: String) {
        repository.registerStore(
            storeName = storeName.trim().lowercase(Locale.ROOT),
            email = email.trim(),
            password = password
        )
    }
}
