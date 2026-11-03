package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.bpsToRate
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
    val paymentMethod: PaymentMethods,
    val terminalName: String? = null,
    val brand: String? = null,
    val installments: Int? = null,
    val tariffRate: BigDecimal = BigDecimal.ZERO.setScale(2),
    val tariffAmount: BigDecimal = BigDecimal.ZERO.setScale(2)
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
    paymentMethod = paymentMethod,
    terminalName = terminalName,
    brand = brand,
    installments = installments,
    tariffRate = tariffRateBps.bpsToRate(),
    tariffAmount = BigDecimal.valueOf(tariffAmountCents, 2)
)

private fun Int.toMoneyAmount(): BigDecimal = BigDecimal.valueOf(toLong(), 2)
