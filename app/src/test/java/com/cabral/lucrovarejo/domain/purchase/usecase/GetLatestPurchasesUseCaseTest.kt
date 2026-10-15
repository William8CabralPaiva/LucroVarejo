package com.cabral.lucrovarejo.domain.purchase.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLatestPurchasesUseCaseTest {
    @Test
    fun returnsLatestPurchasesInRepositoryOrder() = runTest {
        val purchases = listOf(
            PurchaseModel(
                id = "latest",
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
                id = "older",
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
        val useCase = GetLatestPurchasesUseCase(FakePurchaseRepository(purchases))

        assertEquals(purchases, useCase().asSnapshot())
    }

    private class FakePurchaseRepository(
        private val latestPurchases: List<PurchaseModel>
    ) : PurchaseRepository {
        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel = purchase

        override suspend fun getMonthlySummary(
            start: Date,
            end: Date
        ): MonthlyPurchaseSummary = MonthlyPurchaseSummary()

        override fun getLatestPurchases(): Flow<PagingData<PurchaseModel>> =
            flowOf(PagingData.from(latestPurchases))
    }
}
