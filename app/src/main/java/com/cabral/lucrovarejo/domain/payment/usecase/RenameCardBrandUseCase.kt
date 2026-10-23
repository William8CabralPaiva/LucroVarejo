package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.CardBrandRepository
import javax.inject.Inject

class RenameCardBrandUseCase @Inject constructor(
    private val repository: CardBrandRepository
) {
    suspend operator fun invoke(key: String, name: String): CardBrandModel =
        repository.renameCardBrand(key, name)
}
