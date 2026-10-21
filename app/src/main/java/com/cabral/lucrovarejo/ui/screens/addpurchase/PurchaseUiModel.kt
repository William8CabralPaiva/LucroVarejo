package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import java.math.BigDecimal
import java.util.Date

data class PurchaseUiModel(
    val key: String?,
    val cod: String,
    val name: String,
    val cost: BigDecimal,
    val description: String,
    val salePrice: BigDecimal,
    val quantity: Int,
    val saleDate: Date,
    val paymentMethod: PaymentMethods
)

fun PurchaseModel.toUiModel(): PurchaseUiModel = PurchaseUiModel(
    key = key,
    cod = cod,
    name = name,
    cost = cost.toMoneyAmount(),
    description = description,
    salePrice = salePrice.toMoneyAmount(),
    quantity = quantity,
    saleDate = saleDate,
    paymentMethod = paymentMethod
)

private fun Int.toMoneyAmount(): BigDecimal = BigDecimal.valueOf(toLong(), 2)
