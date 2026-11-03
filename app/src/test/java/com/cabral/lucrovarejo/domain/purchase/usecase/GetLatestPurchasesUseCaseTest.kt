package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLatestPurchasesUseCaseTest {
    @Test
    fun returnsLatestPurchasesInRepositoryOrderUsingRequestedLimit() = runTest {
        val purchases = listOf(
            PurchaseModel(
                key = "latest",
                cod = "A-1",
                name = "Mais atual",
                cost = 1000,
                description = "",
                salePrice = 1500,
                quantity = 1,
                saleDate = Date(1_760_000_000_000),
                paymentMethod = PaymentMethods.PIX
            ),
            PurchaseModel(
                key = "older",
                cod = "B-2",
                name = "Anterior",
                cost = 800,
                description = "",
                salePrice = 1200,
                quantity = 2,
                saleDate = Date(1_759_000_000_000),
                paymentMethod = PaymentMethods.CASH
            )
        )
        val repository = FakePurchaseRepository(purchases)

        val result = GetLatestPurchasesUseCase(repository)(4).first()

        assertEquals(purchases, result)
        assertEquals(4, repository.requestedLimit)
    }

    private class FakePurchaseRepository(
        private val latestPurchases: List<PurchaseModel>
    ) : PurchaseRepository {
    override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel = purchase
    override suspend fun deletePurchase(key: String) = Unit
        var requestedLimit: Int? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override fun observeMonthlySummary(
            start: Date,
            end: Date
        ): Flow<MonthlyPurchaseSummary> = flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> {
            requestedLimit = limit
            return flowOf(latestPurchases)
        }
    }
}
