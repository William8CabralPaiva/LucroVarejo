package com.cabral.lucrovarejo.domain.product

interface ProductRepository {
    suspend fun createProduct(product: ProductModel): ProductModel
    suspend fun getProducts(): List<ProductModel>
}
