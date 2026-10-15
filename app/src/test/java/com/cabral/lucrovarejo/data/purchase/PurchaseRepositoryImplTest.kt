package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.cabral.lucrovarejo.data.purchase.model.PurchaseSummaryRecord
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import java.util.Date
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        assertEquals(purchase.copy(id = "generated-purchase-id"), created)
    }

    @Test
    fun calculatesMonthlySalesCostProfitAndQuantityForTimestampRange() = runTest {
        val dataSource = FakePurchaseRemoteDataSource()
        val repository = PurchaseRepositoryImpl(dataSource)
        val start = Date(1_760_000_000_000)
        val end = Date(1_762_678_400_000)
        dataSource.purchases = listOf(
            PurchaseSummaryRecord(cost = 1200, salePrice = 1500, quantity = 3),
            PurchaseSummaryRecord(cost = 500, salePrice = 800, quantity = 2)
        )

        val summary = repository.getMonthlySummary(start, end)

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
    fun mapsLatestPurchaseRecordsToDomainModelsInReturnedOrder() = runTest {
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

        val purchases = PurchaseRepositoryImpl(dataSource).getLatestPurchases()

        assertEquals(
            listOf(
                PurchaseModel(
                    id = "latest-id",
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
                    id = "older-id",
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
    }

    private class FakePurchaseRemoteDataSource : PurchaseRemoteDataSource {
        var savedRequest: PurchaseRequest? = null
        var purchases: List<PurchaseSummaryRecord> = emptyList()
        var requestedRange: Pair<Date, Date>? = null
        var latestPurchases: List<PurchaseRecord> = emptyList()

        override suspend fun createPurchase(request: PurchaseRequest): String {
            savedRequest = request
            return "generated-purchase-id"
        }

        override suspend fun getPurchasesBetween(
            start: Date,
            end: Date
        ): List<PurchaseSummaryRecord> {
            requestedRange = start to end
            return purchases
        }

        override suspend fun getLatestPurchases(): List<PurchaseRecord> = latestPurchases
    }
}
