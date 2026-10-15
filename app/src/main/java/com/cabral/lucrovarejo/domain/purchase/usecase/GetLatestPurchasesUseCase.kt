package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import javax.inject.Inject

class GetLatestPurchasesUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    suspend operator fun invoke(): List<PurchaseModel> = repository.getLatestPurchases()
}
