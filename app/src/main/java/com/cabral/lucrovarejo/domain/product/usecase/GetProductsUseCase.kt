package com.cabral.lucrovarejo.domain.product.usecase

import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<ProductModel>> = repository.observeProducts()
}
