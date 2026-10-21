package com.cabral.lucrovarejo.ui.screens.home

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.GetLatestPurchasesUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.GetMonthlyPurchaseSummaryUseCase
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
        viewModel = HomeViewModel(
            GetMonthlyPurchaseSummaryUseCase(repository),
            GetLatestPurchasesUseCase(repository)
        )
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
        repository.summary.value = summary

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
    fun updatesSummaryAndLatestPurchasesWhenRepositoryEmitsNewData() = runTest {
        advanceUntilIdle()
        val requestCount = repository.summaryRequestCount
        val purchase = purchase("new-sale")

        repository.summary.value = MonthlyPurchaseSummary(totalSales = 123_400)
        repository.latestPurchases.value = listOf(purchase)
        advanceUntilIdle()

        assertEquals(123_400L, viewModel.uiState.value.summary.totalSales)
        assertEquals(listOf(purchase), viewModel.uiState.value.latestPurchases)
        assertFalse(viewModel.uiState.value.isLatestPurchasesLoading)
        assertEquals(requestCount, repository.summaryRequestCount)
        assertEquals(4, repository.requestedLatestLimit)
    }

    @Test
    fun reportsPurchaseFailuresInUiState() = runTest {
        advanceUntilIdle()
        repository.failure = PurchaseFailure(PurchaseFailure.Reason.NETWORK)

        viewModel.onMonthYearChanged(2020, 1)
        advanceUntilIdle()

        assertEquals(R.string.summary_load_error_network, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun reportsLatestPurchasesFailure() = runTest {
        repository.latestFailure = PurchaseFailure(PurchaseFailure.Reason.NETWORK)
        val failingViewModel = HomeViewModel(
            GetMonthlyPurchaseSummaryUseCase(repository),
            GetLatestPurchasesUseCase(repository)
        )

        advanceUntilIdle()

        assertTrue(failingViewModel.uiState.value.hasLatestPurchasesError)
        assertFalse(failingViewModel.uiState.value.isLatestPurchasesLoading)
    }

    @Test
    fun rejectsInvalidMonthAndYear() {
        assertTrue(runCatching { viewModel.onMonthYearChanged(0, 1) }.isFailure)
        assertTrue(runCatching { viewModel.onMonthYearChanged(2026, 13) }.isFailure)
    }

    private fun purchase(id: String) = PurchaseModel(
        key = id,
        cod = "A-1",
        name = "Produto",
        cost = 1000,
        description = "",
        salePrice = 1500,
        quantity = 1,
        saleDate = Date(1_760_000_000_000),
        paymentMethod = PaymentMethods.PIX
    )

    private class FakePurchaseRepository : PurchaseRepository {
    override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel = purchase
    override suspend fun deletePurchase(key: String) = Unit
        val summary = MutableStateFlow(MonthlyPurchaseSummary())
        val latestPurchases = MutableStateFlow<List<PurchaseModel>>(emptyList())
        var failure: PurchaseFailure? = null
        var latestFailure: PurchaseFailure? = null
        var requestedRange: Pair<Date, Date>? = null
        var requestedLatestLimit: Int? = null
        var summaryRequestCount = 0

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override fun observeMonthlySummary(
            start: Date,
            end: Date
        ): Flow<MonthlyPurchaseSummary> {
            summaryRequestCount++
            requestedRange = start to end
            return flow {
                failure?.let { throw it }
                summary.collect { emit(it) }
            }
        }

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> {
            requestedLatestLimit = limit
            return flow {
                latestFailure?.let { throw it }
                latestPurchases.collect { emit(it) }
            }
        }
    }
}
