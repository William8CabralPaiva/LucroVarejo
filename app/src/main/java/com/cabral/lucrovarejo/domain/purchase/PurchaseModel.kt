package com.cabral.lucrovarejo.domain.purchase

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import java.io.Serializable
import java.util.Date

data class PurchaseModel(
    val cod: String,
    val name: String,
    val cost: Int,
    val description: String,
    val salePrice: Int,
    val quantity: Int,
    val saleDate: Date,
    val paymentMethod: PaymentMethods,
    val key: String? = null,
    val terminalId: String? = null,
    val terminalName: String? = null,
    val brand: String? = null,
    val installments: Int? = null,
    val tariffRateBps: Int = 0,
    val tariffId: String? = null,
    val tariffAmountCents: Long = 0L
) : Serializable
