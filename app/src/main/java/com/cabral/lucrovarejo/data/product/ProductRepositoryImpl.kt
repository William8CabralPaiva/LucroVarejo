package com.cabral.lucrovarejo.data.product

import com.cabral.lucrovarejo.data.common.withDataFailure
import com.cabral.lucrovarejo.data.product.datasource.ProductRemoteDataSource
import com.cabral.lucrovarejo.data.product.model.toModel
import com.cabral.lucrovarejo.data.product.model.toRequest
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource
) : ProductRepository {
    override suspend fun createProduct(product: ProductModel): ProductModel {
        val request = product.toRequest()
        val documentId = withDataFailure {
            remoteDataSource.createProduct(request)
        }
        return request.toModel(documentId)
    }

    override suspend fun updateProduct(product: ProductModel): ProductModel {
        val key = requireNotNull(product.key) { "Product key is required to update" }
        val request = product.toRequest()
        withDataFailure { remoteDataSource.updateProduct(key, request) }
        return request.toModel(key)
    }

    override suspend fun deleteProduct(key: String) {
        withDataFailure { remoteDataSource.deleteProduct(key) }
    }

    override fun observeProducts(): Flow<List<ProductModel>> =
        remoteDataSource.observeProducts()
            .map { records -> records.map { it.toModel() } }
            .withDataFailure()
}
