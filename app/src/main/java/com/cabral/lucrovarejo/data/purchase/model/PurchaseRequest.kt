package com.cabral.lucrovarejo.data.purchase.model

import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import java.util.Date

data class PurchaseRequest(
    val cod: String,
    val name: String,
    val cost: Int,
    val description: String,
    val salePrice: Int,
    val quantity: Int,
    val saleDate: Date,
    val paymentMethod: PaymentMethods
) {
    fun toFirestoreFields(): Map<String, Any> = mapOf(
        FIELD_COD to cod,
        FIELD_NAME to name,
        FIELD_COST to cost,
        FIELD_DESCRIPTION to description,
        FIELD_SALE_PRICE to salePrice,
        FIELD_QUANTITY to quantity,
        FIELD_SALE_DATE to saleDate,
        FIELD_PAYMENT_METHOD to paymentMethod.name
    )

    companion object {
        fun from(purchase: PurchaseModel): PurchaseRequest = PurchaseRequest(
            cod = purchase.cod,
            name = purchase.name,
            cost = purchase.cost,
            description = purchase.description,
            salePrice = purchase.salePrice,
            quantity = purchase.quantity,
            saleDate = purchase.saleDate,
            paymentMethod = purchase.paymentMethod
        )

        private const val FIELD_COD = "cod"
        private const val FIELD_NAME = "name"
        private const val FIELD_COST = "cost"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_SALE_PRICE = "sale_price"
        private const val FIELD_QUANTITY = "quantity"
        private const val FIELD_SALE_DATE = "sale_date"
        private const val FIELD_PAYMENT_METHOD = "payment_method"
    }
}
