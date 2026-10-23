package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.CardBrandRepository
import javax.inject.Inject

class ArchiveCardBrandUseCase @Inject constructor(
    private val repository: CardBrandRepository
) {
    suspend operator fun invoke(key: String) = repository.archiveCardBrand(key)
}
