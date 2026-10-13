package com.cabral.lucrovarejo.data.product

import com.cabral.lucrovarejo.data.product.datasource.ProductRemoteDataSource
import com.cabral.lucrovarejo.data.product.model.toModel
import com.cabral.lucrovarejo.data.product.model.toRequest
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource
) : ProductRepository {
    override suspend fun createProduct(product: ProductModel): ProductModel {
        val request = product.toRequest()
        val documentId = remoteDataSource.createProduct(request)
        return request.toModel(documentId)
    }
}
