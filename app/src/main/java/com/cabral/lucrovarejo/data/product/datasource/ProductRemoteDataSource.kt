package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest
import com.cabral.lucrovarejo.data.product.model.ProductRecord
import kotlinx.coroutines.flow.Flow

interface ProductRemoteDataSource {
    suspend fun createProduct(request: ProductRequest): String
    fun observeProducts(): Flow<List<ProductRecord>>
}
