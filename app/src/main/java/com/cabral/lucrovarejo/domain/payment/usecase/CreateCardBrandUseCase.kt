package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.CardBrandRepository
import javax.inject.Inject

class CreateCardBrandUseCase @Inject constructor(
    private val repository: CardBrandRepository
) {
    suspend operator fun invoke(name: String): CardBrandModel =
        repository.createCardBrand(CardBrandModel(name = name))
}
