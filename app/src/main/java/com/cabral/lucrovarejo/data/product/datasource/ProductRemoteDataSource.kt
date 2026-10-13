package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest

interface ProductRemoteDataSource {
    suspend fun createProduct(request: ProductRequest): String
}
