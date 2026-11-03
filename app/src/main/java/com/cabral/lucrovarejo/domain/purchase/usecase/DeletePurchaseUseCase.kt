package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import javax.inject.Inject

class DeletePurchaseUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    suspend operator fun invoke(key: String) = repository.deletePurchase(key)
}
