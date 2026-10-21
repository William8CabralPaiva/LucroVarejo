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
    val paymentMethod: String
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
        paymentMethod = PaymentMethods.valueOf(paymentMethod)
    )
}
