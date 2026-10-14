package com.cabral.lucrovarejo.data.product.model

import com.cabral.lucrovarejo.domain.product.ProductModel
import java.math.BigDecimal
import java.math.RoundingMode

fun ProductModel.toRequest(): ProductRequest = ProductRequest(
    cod = cod,
    name = name,
    cost = cost
        .setScale(MONEY_SCALE, RoundingMode.UNNECESSARY)
        .movePointRight(MONEY_SCALE)
        .intValueExact(),
    description = description
)

fun ProductRequest.toModel(documentId: String): ProductModel = ProductModel(
    id = documentId,
    cod = cod,
    name = name,
    cost = BigDecimal.valueOf(cost.toLong(), MONEY_SCALE),
    description = description
)

fun ProductRecord.toModel(): ProductModel = request.toModel(documentId)

private const val MONEY_SCALE = 2
