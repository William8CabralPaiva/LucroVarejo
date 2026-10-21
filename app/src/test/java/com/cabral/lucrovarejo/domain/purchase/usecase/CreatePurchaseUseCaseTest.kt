package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreatePurchaseUseCaseTest {
    @Test
    fun createsPurchaseThroughRepositoryAndReturnsGeneratedId() = runTest {
        val repository = FakePurchaseRepository()
        val purchase = PurchaseModel(
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 3,
            saleDate = Date(1_735_689_600_000),
            paymentMethod = PaymentMethods.CASH
        )

        val created = CreatePurchaseUseCase(repository)(purchase)

        assertEquals(purchase, repository.createdPurchase)
        assertEquals(purchase.copy(key = "generated-purchase-id"), created)
    }

    private class FakePurchaseRepository : PurchaseRepository {
    override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel = purchase
    override suspend fun deletePurchase(key: String) = Unit
        var createdPurchase: PurchaseModel? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
            createdPurchase = purchase
            return purchase.copy(key = "generated-purchase-id")
        }

        override fun observeMonthlySummary(start: Date, end: Date) =
            flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> =
            flowOf(emptyList())
    }
}
