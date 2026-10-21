package com.cabral.lucrovarejo.domain.product.usecase

import com.cabral.lucrovarejo.domain.product.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(key: String) = repository.deleteProduct(key)
}
