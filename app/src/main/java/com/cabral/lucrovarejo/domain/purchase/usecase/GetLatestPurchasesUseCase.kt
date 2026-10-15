package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import javax.inject.Inject

class GetLatestPurchasesUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    operator fun invoke(): Flow<PagingData<PurchaseModel>> = repository.getLatestPurchases()
}
