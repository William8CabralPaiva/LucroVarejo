package com.cabral.lucrovarejo.data.product.datasource

import com.cabral.lucrovarejo.data.product.model.ProductRequest
import com.cabral.lucrovarejo.data.product.model.ProductRecord

interface ProductRemoteDataSource {
    suspend fun createProduct(request: ProductRequest): String
    suspend fun getProducts(): List<ProductRecord>
}
