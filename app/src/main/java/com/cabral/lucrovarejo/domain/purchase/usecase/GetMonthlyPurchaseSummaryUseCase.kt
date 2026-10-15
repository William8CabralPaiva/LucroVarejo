package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import java.util.Date
import javax.inject.Inject

class GetMonthlyPurchaseSummaryUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    suspend operator fun invoke(start: Date, end: Date): MonthlyPurchaseSummary =
        repository.getMonthlySummary(start, end)
}
