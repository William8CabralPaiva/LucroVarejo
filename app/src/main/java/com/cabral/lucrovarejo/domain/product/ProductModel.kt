package com.cabral.lucrovarejo.domain.product

import java.math.BigDecimal

data class ProductModel(
    val id: String? = null,
    val cod: String,
    val name: String,
    val cost: BigDecimal,
    val description: String
)
