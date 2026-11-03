package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetLatestPurchasesUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    operator fun invoke(limit: Int): Flow<List<PurchaseModel>> =
        repository.observeLatestPurchases(limit)
}