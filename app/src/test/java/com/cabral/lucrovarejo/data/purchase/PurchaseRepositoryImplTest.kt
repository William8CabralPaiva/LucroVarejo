package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import com.cabral.lucrovarejo.domain.purchase.PurchaseFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseRepositoryImplTest {
    @Test
    fun savesAllPurchaseFieldsAndReturnsGeneratedId() = runTest {
        val dataSource = FakePurchaseRemoteDataSource()
        val repository = PurchaseRepositoryImpl(dataSource)
        val saleDate = Date(1_735_689_600_000)
        val paymentMethod = PaymentMethods.PIX
        val purchase = PurchaseModel(
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 3,
            saleDate = saleDate,
            paymentMethod = paymentMethod
        )

        val created = repository.createPurchase(purchase)

        assertEquals(
            PurchaseRequest(
                cod = "A-1",
                name = "Produto",
                cost = 1200,
                description = "Descricao",
                salePrice = 1500,
                quantity = 3,
                saleDate = saleDate,
                paymentMethod = paymentMethod
            ),
            dataSource.savedRequest
        )
        assertEquals(purchase.copy(key = "generated-purchase-id"), created)
    }

    @Test
    fun calculatesMonthlySalesCostProfitAndQuantityForTimestampRange() = runTest {
        val dataSource = FakePurchaseRemoteDataSource()
        val repository = PurchaseRepositoryImpl(dataSource)
        val start = Date(1_760_000_000_000)
        val end = Date(1_762_678_400_000)
        dataSource.purchases.value = listOf(
            PurchaseSummaryRecord(cost = 1200, salePrice = 1500, quantity = 3),
            PurchaseSummaryRecord(cost = 500, salePrice = 800, quantity = 2)
        )

        val summary = repository.observeMonthlySummary(start, end).first()

        assertEquals(start to end, dataSource.requestedRange)
        assertEquals(
            MonthlyPurchaseSummary(
                totalSales = 6100,
                totalCost = 4600,
                totalProfit = 1500,
                productsSold = 5
            ),
            summary
        )
    }

    @Test
    fun mapsLatestPurchaseRecordsToDomainModelsInReturnedOrderUsingLimit() = runTest {
        val firstDate = Date(1_760_000_000_000)
        val secondDate = Date(1_759_000_000_000)
        val dataSource = FakePurchaseRemoteDataSource().apply {
            latestPurchases = listOf(
                PurchaseRecord(
                    documentId = "latest-id",
                    cod = "A-1",
                    name = "Produto mais atual",
                    cost = 1200,
                    description = "Descricao",
                    salePrice = 1500,
                    quantity = 2,
                    saleDate = firstDate,
                    paymentMethod = "PIX"
                ),
                PurchaseRecord(
                    documentId = "older-id",
                    cod = "B-2",
                    name = "Produto anterior",
                    cost = 800,
                    description = "Outra descricao",
                    salePrice = 1000,
                    quantity = 1,
                    saleDate = secondDate,
                    paymentMethod = "CASH"
                )
            )
        }

        val purchases = PurchaseRepositoryImpl(dataSource).observeLatestPurchases(4).first()

        assertEquals(
            listOf(
                PurchaseModel(
                    key = "latest-id",
                    cod = "A-1",
                    name = "Produto mais atual",
                    cost = 1200,
                    description = "Descricao",
                    salePrice = 1500,
                    quantity = 2,
                    saleDate = firstDate,
                    paymentMethod = PaymentMethods.PIX
                ),
                PurchaseModel(
                    key = "older-id",
                    cod = "B-2",
                    name = "Produto anterior",
                    cost = 800,
                    description = "Outra descricao",
                    salePrice = 1000,
                    quantity = 1,
                    saleDate = secondDate,
                    paymentMethod = PaymentMethods.CASH
                )
            ),
            purchases
        )
        assertEquals(4, dataSource.requestedLimit)
    }

    @Test
    fun recalculatesSummaryWhenRemoteDataChanges() = runTest {
        val dataSource = FakePurchaseRemoteDataSource()
        val repository = PurchaseRepositoryImpl(dataSource)
        val firstSnapshot = listOf(PurchaseSummaryRecord(cost = 1000, salePrice = 1500, quantity = 1))
        val secondSnapshot = firstSnapshot +
            PurchaseSummaryRecord(cost = 500, salePrice = 800, quantity = 2)
        dataSource.purchases.value = firstSnapshot

        val summaries = mutableListOf<MonthlyPurchaseSummary>()
        repository.observeMonthlySummary(Date(1_760_000_000_000), Date(1_762_678_400_000))
            .take(2)
            .collect {
                summaries += it
                if (summaries.size == 1) dataSource.purchases.value = secondSnapshot
            }

        assertEquals(
            listOf(
                MonthlyPurchaseSummary(1500, 1000, 500, 1),
                MonthlyPurchaseSummary(3100, 2000, 1100, 3)
            ),
            summaries
        )
    }

    @Test
    fun mapsRemoteFailuresToPurchaseFailure() = runTest {
        val dataSource = FakePurchaseRemoteDataSource().apply {
            error = IllegalStateException("boom")
        }

        val failure = runCatching {
            PurchaseRepositoryImpl(dataSource).observeLatestPurchases(4).first()
        }.exceptionOrNull()

        assertTrue(failure is PurchaseFailure)
        assertEquals(PurchaseFailure.Reason.UNKNOWN, (failure as PurchaseFailure).reason)
    }

    @Test
    fun updatesAndDeletesPurchaseUsingItsExistingKey() = runTest {
        val dataSource = FakePurchaseRemoteDataSource()
        val purchase = PurchaseModel(
            key = "purchase-1",
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 3,
            saleDate = Date(1_735_689_600_000),
            paymentMethod = PaymentMethods.PIX
        )
        val repository = PurchaseRepositoryImpl(dataSource)

        assertEquals(purchase, repository.updatePurchase(purchase))
        repository.deletePurchase("purchase-1")

        assertEquals("purchase-1", dataSource.updatedKey)
        assertEquals(PurchaseRequest.from(purchase), dataSource.updatedRequest)
        assertEquals("purchase-1", dataSource.deletedKey)
    }

    @Test
    fun rejectsMissingKeyAndInvalidFlowArguments() = runTest {
        val repository = PurchaseRepositoryImpl(FakePurchaseRemoteDataSource())
        val purchaseWithoutKey = PurchaseModel(
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 1,
            saleDate = Date(),
            paymentMethod = PaymentMethods.CASH
        )

        assertTrue(runCatching { repository.updatePurchase(purchaseWithoutKey) }.exceptionOrNull()
            is IllegalArgumentException)
        assertTrue(runCatching { repository.observeLatestPurchases(0) }.exceptionOrNull()
            is IllegalArgumentException)
        assertTrue(runCatching { repository.observeMonthlySummary(Date(2), Date(1)) }
            .exceptionOrNull() is IllegalArgumentException)
    }

    private class FakePurchaseRemoteDataSource : PurchaseRemoteDataSource {
        var updatedKey: String? = null
        var updatedRequest: PurchaseRequest? = null
        var deletedKey: String? = null

        override suspend fun updatePurchase(key: String, request: PurchaseRequest) {
            updatedKey = key
            updatedRequest = request
        }

        override suspend fun deletePurchase(key: String) {
            deletedKey = key
        }

        var savedRequest: PurchaseRequest? = null
        val purchases = MutableStateFlow<List<PurchaseSummaryRecord>>(emptyList())
        var requestedRange: Pair<Date, Date>? = null
        var latestPurchases: List<PurchaseRecord> = emptyList()
        var requestedLimit: Int? = null
        var error: Exception? = null

        override suspend fun createPurchase(request: PurchaseRequest): String {
            savedRequest = request
            return "generated-purchase-id"
        }

        override fun observePurchasesBetween(
            start: Date,
            end: Date
        ): Flow<List<PurchaseSummaryRecord>> {
            requestedRange = start to end
            return purchases
        }

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseRecord>> {
            requestedLimit = limit
            return flow {
                error?.let { throw it }
                emit(latestPurchases)
            }
        }
    }
}
