package com.cabral.lucrovarejo.ui.screens.home

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePurchaseRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakePurchaseRepository()
        viewModel = HomeViewModel(GetMonthlyPurchaseSummaryUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsSummaryForSelectedMonthUsingHalfOpenDateRange() = runTest {
        advanceUntilIdle()
        val summary = MonthlyPurchaseSummary(
            totalSales = 845_000,
            totalCost = 520_000,
            totalProfit = 325_000,
            productsSold = 32
        )
        repository.summary = summary

        viewModel.onMonthYearChanged(2026, 10)
        advanceUntilIdle()

        val start = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 1, 0, 0, 0)
        }.time
        val end = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.NOVEMBER, 1, 0, 0, 0)
        }.time
        assertEquals(start to end, repository.requestedRange)
        assertEquals(summary, viewModel.uiState.value.summary)
        assertEquals(2026, viewModel.uiState.value.year)
        assertEquals(10, viewModel.uiState.value.month)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun reportsPurchaseFailuresInUiState() = runTest {
        advanceUntilIdle()
        repository.failure = PurchaseFailure(PurchaseFailure.Reason.NETWORK)

        viewModel.onMonthYearChanged(2026, 10)
        advanceUntilIdle()

        assertEquals(R.string.summary_load_error_network, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun rejectsInvalidMonthAndYear() {
        assertTrue(runCatching { viewModel.onMonthYearChanged(0, 1) }.isFailure)
        assertTrue(runCatching { viewModel.onMonthYearChanged(2026, 13) }.isFailure)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        var summary = MonthlyPurchaseSummary()
        var failure: PurchaseFailure? = null
        var requestedRange: Pair<Date, Date>? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override suspend fun getMonthlySummary(
            start: Date,
            end: Date
        ): MonthlyPurchaseSummary {
            requestedRange = start to end
            failure?.let { throw it }
            return summary
        }

        override suspend fun getLatestPurchases() = emptyList<PurchaseModel>()
    }
}
