package com.cabral.lucrovarejo.data.purchase

import com.cabral.lucrovarejo.data.purchase.datasource.PurchaseRemoteDataSource
import com.cabral.lucrovarejo.data.purchase.model.PurchaseRequest
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
        val paymentMethods = listOf(PaymentMethods.CASH, PaymentMethods.PIX)
        val purchase = PurchaseModel(
            cod = "A-1",
            name = "Produto",
            cost = 1200,
            description = "Descricao",
            salePrice = 1500,
            quantity = 3,
            saleDate = saleDate,
            paymentMethods = paymentMethods
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
                paymentMethods = paymentMethods
            ),
            dataSource.savedRequest
        )
        assertEquals(purchase.copy(id = "generated-purchase-id"), created)
    }

    private class FakePurchaseRemoteDataSource : PurchaseRemoteDataSource {
        var savedRequest: PurchaseRequest? = null

        override suspend fun createPurchase(request: PurchaseRequest): String {
            savedRequest = request
            return "generated-purchase-id"
        }
    }
}
