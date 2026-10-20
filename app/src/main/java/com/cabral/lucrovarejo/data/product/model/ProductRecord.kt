package com.cabral.lucrovarejo.data.product.model

import java.util.Date

data class ProductRecord(
    val documentId: String,
    val request: ProductRequest,
    val createdAt: Date? = null,
)
