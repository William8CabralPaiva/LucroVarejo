package com.cabral.lucrovarejo.domain.product

import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun createProduct(product: ProductModel): ProductModel
    fun observeProducts(): Flow<List<ProductModel>>
}
