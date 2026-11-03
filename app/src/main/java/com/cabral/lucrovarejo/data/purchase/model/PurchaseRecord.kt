package com.cabral.lucrovarejo.data.purchase.model

import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import java.util.Date

data class PurchaseRecord(
    val documentId: String,
    val cod: String,
    val name: String,
    val cost: Int,
    val description: String,
    val salePrice: Int,
    val quantity: Int,
    val saleDate: Date,
    val paymentMethod: String,
    val terminalId: String? = null,
    val terminalName: String? = null,
    val brand: String? = null,
    val installments: Int? = null,
    val tariffRateBps: Int = 0,
    val tariffId: String? = null,
    val tariffAmountCents: Long = 0L
) {
    fun toModel(): PurchaseModel = PurchaseModel(
        key = documentId,
        cod = cod,
        name = name,
        cost = cost,
        description = description,
        salePrice = salePrice,
        quantity = quantity,
        saleDate = saleDate,
        paymentMethod = PaymentMethods.valueOf(paymentMethod),
        terminalId = terminalId,
        terminalName = terminalName,
        brand = brand,
        installments = installments,
        tariffRateBps = tariffRateBps,
        tariffId = tariffId,
        tariffAmountCents = tariffAmountCents
    )
}
