package com.cabral.lucrovarejo.domain.product.usecase

import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateProductUseCaseTest {
    @Test
    fun createsProductThroughRepositoryAndReturnsGeneratedId() = runTest {
        val repository = FakeProductRepository()
        val product = ProductModel(
            cod = "A-1",
            name = "Produto",
            cost = BigDecimal("12.34"),
            description = "Descricao"
        )

        val created = CreateProductUseCase(repository)(product)

        assertEquals(product, repository.createdProduct)
        assertEquals("generated-product-id", created.id)
        assertEquals(product.copy(id = "generated-product-id"), created)
    }

    private class FakeProductRepository : ProductRepository {
        var createdProduct: ProductModel? = null

        override suspend fun createProduct(product: ProductModel): ProductModel {
            createdProduct = product
            return product.copy(id = "generated-product-id")
        }

        override suspend fun getProducts(): List<ProductModel> = emptyList()
    }
}
