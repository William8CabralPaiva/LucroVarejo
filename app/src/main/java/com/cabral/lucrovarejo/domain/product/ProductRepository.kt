package com.cabral.lucrovarejo.domain.product

import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun createProduct(product: ProductModel): ProductModel
    suspend fun updateProduct(product: ProductModel): ProductModel
    suspend fun deleteProduct(key: String)
    fun observeProducts(): Flow<List<ProductModel>>
}
