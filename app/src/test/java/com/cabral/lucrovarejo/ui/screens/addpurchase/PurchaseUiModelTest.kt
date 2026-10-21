package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import java.math.BigDecimal
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseUiModelTest {
    @Test
    fun convertsPurchaseMoneyFromCentsAndPreservesOtherFields() {
        val saleDate = Date(1_735_689_600_000)
        val paymentMethod = PaymentMethods.PIX
        val purchase = PurchaseModel(
            key = "purchase-1",
            cod = "A-1",
            name = "Produto",
            cost = 1234,
            description = "Descricao",
            salePrice = 1599,
            quantity = 3,
            saleDate = saleDate,
            paymentMethod = paymentMethod
        )

        assertEquals(
            PurchaseUiModel(
                key = "purchase-1",
                cod = "A-1",
                name = "Produto",
                cost = BigDecimal("12.34"),
                description = "Descricao",
                salePrice = BigDecimal("15.99"),
                quantity = 3,
                saleDate = saleDate,
                paymentMethod = paymentMethod
            ),
            purchase.toUiModel()
        )
    }
}
