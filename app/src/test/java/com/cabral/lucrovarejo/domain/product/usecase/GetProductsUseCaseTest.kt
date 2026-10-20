package com.cabral.lucrovarejo.domain.product.usecase

import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProductsUseCaseTest {
    @Test
    fun returnsAllProductsFromRepository() = runTest {
        val products = listOf(
            ProductModel(
                id = "product-1",
                cod = "A-1",
                name = "Produto 1",
                cost = BigDecimal("12.34"),
                description = "Descricao 1"
            ),
            ProductModel(
                id = "product-2",
                cod = "B-2",
                name = "Produto 2",
                cost = BigDecimal("56.78"),
                description = "Descricao 2"
            )
        )
        val useCase = GetProductsUseCase(FakeProductRepository(products))

        assertEquals(products, useCase().first())
    }

    private class FakeProductRepository(
        private val products: List<ProductModel>
    ) : ProductRepository {
        override suspend fun createProduct(product: ProductModel): ProductModel = product

        override fun observeProducts() = flowOf(products)
    }
}
