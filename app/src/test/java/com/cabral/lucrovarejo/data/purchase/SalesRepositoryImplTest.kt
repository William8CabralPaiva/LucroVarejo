package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.SalesRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.SalesCursor
import com.cabral.lucrovarejo.domain.purchase.SalesFilter
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SalesRepositoryImplTest {
    @Test
    fun returnsFirstPageOfTenWithCursorOnLastItem() = runTest {
        val source = FakeSource(records(25))
        val page = SalesRepositoryImpl(source).getSalesPage(SalesFilter())

        assertEquals((25 downTo 16).map { "p$it" }, page.items.map { it.name })
        assertEquals("d16", page.nextCursor?.documentId)
        assertEquals(10, source.limits.single())
    }

    @Test
    fun secondPageContinuesAfterCursorAndEndsWithoutCursor() = runTest {
        val repository = SalesRepositoryImpl(FakeSource(records(15)))
        val first = repository.getSalesPage(SalesFilter())
        val second = repository.getSalesPage(SalesFilter(), first.nextCursor)

        assertEquals((5 downTo 1).map { "p$it" }, second.items.map { it.name })
        assertNull(second.nextCursor)
    }

    @Test
    fun nameSearchIgnoresCaseAndAccentsAndFillsPageAcrossBatches() = runTest {
        val all = (1..40).map { record(it, if (it % 2 == 0) "Bolsa Pequena $it" else "Camisa $it") } +
            record(41, "BOLSÁ grande")
        val page = SalesRepositoryImpl(FakeSource(all.sortedByDescending { it.saleDate }))
            .getSalesPage(SalesFilter(nameQuery = "bolsa"))

        assertEquals(10, page.items.size)
        assertEquals("BOLSÁ grande", page.items.first().name)
        assertNotNull(page.nextCursor)
    }

    @Test
    fun passesDateRangeToDataSource() = runTest {
        val start = Date(1_000)
        val end = Date(2_000)
        val source = FakeSource(emptyList())
        SalesRepositoryImpl(source).getSalesPage(SalesFilter(dateStart = start, dateEnd = end))

        assertEquals(start, source.lastStart)
        assertEquals(end, source.lastEnd)
    }

    private fun records(count: Int) = (count downTo 1).map { record(it, "p$it") }

    private fun record(index: Int, name: String) = PurchaseRecord(
        documentId = "d$index",
        cod = "c$index",
        name = name,
        cost = 100,
        description = "",
        salePrice = 200,
        quantity = 1,
        saleDate = Date(index * 1_000L),
        paymentMethod = PaymentMethods.entries.first().name
    )

    private class FakeSource(private val sorted: List<PurchaseRecord>) : SalesRemoteDataSource {
        val limits = mutableListOf<Int>()
        var lastStart: Date? = null
        var lastEnd: Date? = null

        override suspend fun fetchSales(
            dateStart: Date?,
            dateEnd: Date?,
            after: SalesCursor?,
            limit: Int
        ): List<PurchaseRecord> {
            limits += limit
            lastStart = dateStart
            lastEnd = dateEnd
            val startIndex = after?.let { c -> sorted.indexOfFirst { it.documentId == c.documentId } + 1 } ?: 0
            return sorted.drop(startIndex).take(limit)
        }
    }
}