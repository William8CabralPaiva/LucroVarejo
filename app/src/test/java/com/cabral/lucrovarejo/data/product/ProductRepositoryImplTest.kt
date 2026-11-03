package com.cabral.lucrovarejo.data.product

import com.cabral.lucrovarejo.data.product.datasource.ProductRemoteDataSource
import com.cabral.lucrovarejo.data.product.model.ProductRecord
import com.cabral.lucrovarejo.data.product.model.ProductRequest
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryImplTest {
    @Test
    fun createsUpdatesAndDeletesProductsUsingTheirFirestoreRepresentation() = runTest {
        val dataSource = FakeProductRemoteDataSource()
        val repository = ProductRepositoryImpl(dataSource)
        val product = product()

        val created = repository.createProduct(product)
        val updated = repository.updateProduct(product.copy(key = "product-1", name = "Atualizado"))
        repository.deleteProduct("product-1")

        assertEquals(product.copy(key = "created-id"), created)
        assertEquals(
            ProductRequest("A-1", "Produto", 1234, "Descricao"),
            dataSource.createdRequest
        )
        assertEquals("product-1", dataSource.updatedKey)
        assertEquals(
            ProductRequest("A-1", "Atualizado", 1234, "Descricao"),
            dataSource.updatedRequest
        )
        assertEquals("product-1", dataSource.deletedKey)
        assertEquals(product.copy(key = "product-1", name = "Atualizado"), updated)
    }

    @Test
    fun requiresAKeyBeforeUpdatingAProduct() = runTest {
        val failure = runCatching {
            ProductRepositoryImpl(FakeProductRemoteDataSource()).updateProduct(product())
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertEquals("Product key is required to update", failure?.message)
    }

    @Test
    fun mapsRemoteRecordsAndUnexpectedFailures() = runTest {
        val dataSource = FakeProductRemoteDataSource().apply {
            records = listOf(
                ProductRecord(
                    documentId = "product-1",
                    request = ProductRequest("A-1", "Produto", 1234, "Descricao")
                )
            )
        }
        val repository = ProductRepositoryImpl(dataSource)

        assertEquals(listOf(product(key = "product-1")), repository.observeProducts().first())

        dataSource.failure = IllegalStateException("unavailable")
        val failure = runCatching { repository.observeProducts().first() }.exceptionOrNull()

        assertTrue(failure is ProductFailure)
        assertEquals(ProductFailure.Reason.UNKNOWN, (failure as ProductFailure).reason)
    }

    private fun product(key: String? = null) = ProductModel(
        key = key,
        cod = "A-1",
        name = "Produto",
        cost = BigDecimal("12.34"),
        description = "Descricao"
    )

    private class FakeProductRemoteDataSource : ProductRemoteDataSource {
        var createdRequest: ProductRequest? = null
        var updatedKey: String? = null
        var updatedRequest: ProductRequest? = null
        var deletedKey: String? = null
        var records = emptyList<ProductRecord>()
        var failure: Exception? = null

        override suspend fun createProduct(request: ProductRequest): String {
            createdRequest = request
            return "created-id"
        }

        override suspend fun updateProduct(key: String, request: ProductRequest) {
            updatedKey = key
            updatedRequest = request
        }

        override suspend fun deleteProduct(key: String) {
            deletedKey = key
        }

        override fun observeProducts(): Flow<List<ProductRecord>> = flow {
            failure?.let { throw it }
            emit(records)
        }
    }
}
