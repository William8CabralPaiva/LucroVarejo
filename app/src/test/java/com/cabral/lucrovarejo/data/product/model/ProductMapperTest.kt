package com.cabral.lucrovarejo.data.product.model

import com.cabral.lucrovarejo.domain.product.ProductModel
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductMapperTest {
    @Test
    fun convertsAppMoneyToIntegerCentsInRequestAndFirestoreFields() {
        val model = ProductModel(
            cod = "A-1",
            name = "Produto",
            cost = BigDecimal("12.34"),
            description = "Descricao"
        )

        val request = model.toRequest()

        assertEquals(1234, request.cost)
        assertEquals(1234, request.toFirestoreFields()["cost"])
        assertTrue(request.toFirestoreFields()["cost"] is Int)
    }

    @Test
    fun convertsRequestCentsBackToAppMoney() {
        val model = ProductRequest(
            cod = "A-1",
            name = "Produto",
            cost = 1234,
            description = "Descricao"
        ).toModel("product-id")

        assertEquals("product-id", model.id)
        assertEquals(BigDecimal("12.34"), model.cost)
    }

    @Test
    fun rejectsFractionsSmallerThanOneCentInsteadOfRounding() {
        val model = ProductModel(
            cod = "A-1",
            name = "Produto",
            cost = BigDecimal("12.345"),
            description = "Descricao"
        )

        val failure = runCatching { model.toRequest() }.exceptionOrNull()

        assertTrue(failure is ArithmeticException)
    }

    @Test
    fun rejectsCentsOutsideIntegerRange() {
        val model = ProductModel(
            cod = "A-1",
            name = "Produto",
            cost = BigDecimal("21474836.48"),
            description = "Descricao"
        )

        val failure = runCatching { model.toRequest() }.exceptionOrNull()

        assertTrue(failure is ArithmeticException)
    }
}
