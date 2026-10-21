package com.cabral.lucrovarejo.ui.screens.sales

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesPage
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.GetSalesPageUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.ObserveSalesChangesUseCase
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SalesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsFirstPageThenNextPageUntilEnd() = runTest(dispatcher) {
        val repository = FakeRepository(pages = mutableListOf(page(10, true), page(3, false)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(10, viewModel.uiState.value.items.size)
        assertFalse(viewModel.uiState.value.endReached)

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(13, viewModel.uiState.value.items.size)
        assertTrue(viewModel.uiState.value.endReached)
        assertEquals(CURSOR, repository.cursors[1])
    }

    @Test
    fun searchIsDebouncedAndRestartsFromFirstPage() = runTest(dispatcher) {
        val repository = FakeRepository(pages = mutableListOf(page(10, true), page(2, false)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onQueryChange("bol")
        viewModel.onQueryChange("bolsa")
        advanceTimeBy(200)
        assertEquals(1, repository.filters.size)
        advanceUntilIdle()

        assertEquals(2, repository.filters.size)
        assertEquals("bolsa", repository.filters.last().nameQuery)
        assertEquals(null, repository.cursors.last())
        assertEquals(2, viewModel.uiState.value.items.size)
    }

    @Test
    fun dateFilterSendsOneDayRange() = runTest(dispatcher) {
        val repository = FakeRepository(pages = mutableListOf(page(1, false)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onDateSelected(1_759_708_800_000L)
        advanceUntilIdle()

        val filter = repository.filters.last()
        assertEquals(24 * 60 * 60 * 1000L, filter.dateEnd!!.time - filter.dateStart!!.time)
    }

    @Test
    fun failureShowsErrorAndRetryRecovers() = runTest(dispatcher) {
        val repository = FakeRepository(pages = mutableListOf(page(2, false)), failuresLeft = 1)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)

        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(2, viewModel.uiState.value.items.size)
    }

    @Test
    fun reloadsSalesWhenFirestoreEmitsAChange() = runTest(dispatcher) {
        val repository = FakeRepository(pages = mutableListOf(page(1, false), page(2, false)))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.emitSalesChange()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.items.size)
    }

    private fun createViewModel(repository: SalesRepository) = SalesViewModel(
        getSalesPage = GetSalesPageUseCase(repository),
        observeSalesChanges = ObserveSalesChangesUseCase(repository)
    )

    private fun page(size: Int, hasMore: Boolean) = SalesPage(
        items = (1..size).map {
            PurchaseModel(
                key = "id$it",
                cod = "c",
                name = "p$it",
                cost = 100,
                description = "",
                salePrice = 200,
                quantity = 1,
                saleDate = Date(),
                paymentMethod = PaymentMethods.entries.first()
            )
        },
        nextCursor = if (hasMore) CURSOR else null
    )

    private class FakeRepository(
        private val pages: MutableList<SalesPage>,
        private var failuresLeft: Int = 0
    ) : SalesRepository {
        val filters = mutableListOf<SalesFilter>()
        val cursors = mutableListOf<SalesCursor?>()
        private val salesChanges = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

        override suspend fun getSalesPage(
            filter: SalesFilter,
            cursor: SalesCursor?,
            pageSize: Int
        ): SalesPage {
            filters += filter
            cursors += cursor
            if (failuresLeft > 0) {
                failuresLeft--
                error("boom")
            }
            return if (pages.size > 1) pages.removeAt(0) else pages.first()
        }

        override fun observeSalesChanges(filter: SalesFilter): Flow<Unit> = salesChanges

        fun emitSalesChange() {
            salesChanges.tryEmit(Unit)
        }
    }

    private companion object {
        val CURSOR = SalesCursor(Date(0), "last")
    }
}