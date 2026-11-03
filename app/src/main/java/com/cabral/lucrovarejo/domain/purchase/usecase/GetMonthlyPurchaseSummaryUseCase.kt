package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMonthlyPurchaseSummaryUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    operator fun invoke(start: Date, end: Date): Flow<MonthlyPurchaseSummary> =
        repository.observeMonthlySummary(start, end)
}