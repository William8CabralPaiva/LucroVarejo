package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.SALES_PAGE_SIZE
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import com.cabral.lucrovarejo.domain.purchase.SalesPage
import com.cabral.lucrovarejo.domain.purchase.SalesRepository
import java.util.Date
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GetSalesPageUseCaseTest {
    @Test
    fun usesDefaultPageSizeAndReturnsRepositoryPage() = runTest {
        val expected = SalesPage(emptyList(), nextCursor = null)
        val repository = FakeSalesRepository(result = expected)
        val filter = SalesFilter(nameQuery = "bolsa")

        val page = GetSalesPageUseCase(repository)(filter)

        assertSame(expected, page)
        assertEquals(filter, repository.filter)
        assertEquals(null, repository.cursor)
        assertEquals(SALES_PAGE_SIZE, repository.pageSize)
    }

    @Test
    fun forwardsCursorAndCustomPageSize() = runTest {
        val repository = FakeSalesRepository(result = SalesPage(emptyList(), null))
        val cursor = SalesCursor(Date(1_000), "doc")

        GetSalesPageUseCase(repository)(SalesFilter(), cursor, pageSize = 5)

        assertEquals(cursor, repository.cursor)
        assertEquals(5, repository.pageSize)
    }

    @Test
    fun propagatesRepositoryFailure() = runTest {
        val failure = IllegalStateException("boom")
        val repository = FakeSalesRepository(error = failure)

        try {
            GetSalesPageUseCase(repository)(SalesFilter())
            fail("Expected failure")
        } catch (thrown: IllegalStateException) {
            assertTrue(thrown === failure)
        }
    }

    private class FakeSalesRepository(
        private val result: SalesPage? = null,
        private val error: Throwable? = null
    ) : SalesRepository {
        var filter: SalesFilter? = null
        var cursor: SalesCursor? = null
        var pageSize: Int? = null

        override suspend fun getSalesPage(
            filter: SalesFilter,
            cursor: SalesCursor?,
            pageSize: Int
        ): SalesPage {
            this.filter = filter
            this.cursor = cursor
            this.pageSize = pageSize
            error?.let { throw it }
            return result!!
        }

        override fun observeSalesChanges(filter: SalesFilter) = flowOf(Unit)
    }
}
