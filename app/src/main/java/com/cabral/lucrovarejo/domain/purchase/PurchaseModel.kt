package com.cabral.lucrovarejo.domain.purchase

import java.util.Date

data class PurchaseModel(
    val id: String? = null,
    val cod: String,
    val name: String,
    val cost: Int,
    val description: String,
    val salePrice: Int,
    val quantity: Int,
    val saleDate: Date
)
