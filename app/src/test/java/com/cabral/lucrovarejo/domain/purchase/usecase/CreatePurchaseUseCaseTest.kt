package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import java.util.Date
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
        assertEquals(purchase.copy(id = "generated-purchase-id"), created)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        var createdPurchase: PurchaseModel? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
            createdPurchase = purchase
            return purchase.copy(id = "generated-purchase-id")
        }
    }
}
