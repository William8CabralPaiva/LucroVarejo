package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import androidx.paging.PagingData
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMonthlyPurchaseSummaryUseCaseTest {
    @Test
    fun requestsMonthlySummaryForGivenTimestampRange() = runTest {
        val repository = FakePurchaseRepository()
        val start = Date(1_760_000_000_000)
        val end = Date(1_762_678_400_000)
        val summary = MonthlyPurchaseSummary(
            totalSales = 4500,
            totalCost = 3600,
            totalProfit = 900,
            productsSold = 3
        )
        repository.summary = summary

        val result = GetMonthlyPurchaseSummaryUseCase(repository)(start, end)

        assertEquals(start to end, repository.requestedRange)
        assertEquals(summary, result)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        var summary = MonthlyPurchaseSummary()
        var requestedRange: Pair<Date, Date>? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override suspend fun getMonthlySummary(start: Date, end: Date): MonthlyPurchaseSummary {
            requestedRange = start to end
            return summary
        }

        override fun getLatestPurchases(): Flow<PagingData<PurchaseModel>> =
            flowOf(PagingData.empty())
    }
}
