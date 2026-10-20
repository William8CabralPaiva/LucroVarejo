package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMonthlyPurchaseSummaryUseCaseTest {
    @Test
    fun observesMonthlySummaryForGivenTimestampRangeAndEmitsEveryUpdate() = runTest {
        val repository = FakePurchaseRepository()
        val start = Date(1_760_000_000_000)
        val end = Date(1_762_678_400_000)
        val first = MonthlyPurchaseSummary(
            totalSales = 4500,
            totalCost = 3600,
            totalProfit = 900,
            productsSold = 3
        )
        val second = first.copy(totalSales = 6000, totalProfit = 2400, productsSold = 4)
        repository.summary.value = first

        val flow = GetMonthlyPurchaseSummaryUseCase(repository)(start, end)
        val results = mutableListOf<MonthlyPurchaseSummary>()
        flow.take(2).collect {
            results += it
            if (results.size == 1) repository.summary.value = second
        }

        assertEquals(start to end, repository.requestedRange)
        assertEquals(listOf(first, second), results)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        val summary = MutableStateFlow(MonthlyPurchaseSummary())
        var requestedRange: Pair<Date, Date>? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override fun observeMonthlySummary(
            start: Date,
            end: Date
        ): Flow<MonthlyPurchaseSummary> {
            requestedRange = start to end
            return summary
        }

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> =
            flowOf(emptyList())
    }
}