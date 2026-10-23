package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.CardBrandRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCardBrandsUseCase @Inject constructor(
    private val repository: CardBrandRepository
) {
    operator fun invoke(): Flow<List<CardBrandModel>> = repository.observeCardBrands()
}
