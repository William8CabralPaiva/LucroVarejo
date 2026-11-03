package com.cabral.lucrovarejo.domain.product

import java.io.Serializable
import java.math.BigDecimal

data class ProductModel(
    val cod: String,
    val name: String,
    val cost: BigDecimal,
    val description: String,
    val key: String? = null
) : Serializable
