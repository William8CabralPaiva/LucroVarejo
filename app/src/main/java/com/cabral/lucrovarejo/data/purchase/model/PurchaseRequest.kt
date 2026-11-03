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
    val paymentMethod: PaymentMethods,
    val terminalId: String? = null,
    val terminalName: String? = null,
    val brand: String? = null,
    val installments: Int? = null,
    val tariffRateBps: Int = 0,
    val tariffId: String? = null,
    val tariffAmountCents: Long = 0L
) {
    // Campos opcionais vão como null para o update sobrescrever valores antigos (ex.: trocar Pix por dinheiro).
    fun toFirestoreFields(): Map<String, Any?> = mapOf(
        FIELD_COD to cod,
        FIELD_NAME to name,
        FIELD_COST to cost,
        FIELD_DESCRIPTION to description,
        FIELD_SALE_PRICE to salePrice,
        FIELD_QUANTITY to quantity,
        FIELD_SALE_DATE to saleDate,
        FIELD_PAYMENT_METHOD to paymentMethod.name,
        FIELD_TERMINAL_ID to terminalId,
        FIELD_TERMINAL_NAME to terminalName,
        FIELD_BRAND to brand,
        FIELD_INSTALLMENTS to installments,
        FIELD_TARIFF_RATE_BPS to tariffRateBps,
        FIELD_TARIFF_ID to tariffId,
        FIELD_TARIFF_AMOUNT_CENTS to tariffAmountCents
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
            paymentMethod = purchase.paymentMethod,
            terminalId = purchase.terminalId,
            terminalName = purchase.terminalName,
            brand = purchase.brand,
            installments = purchase.installments,
            tariffRateBps = purchase.tariffRateBps,
            tariffId = purchase.tariffId,
            tariffAmountCents = purchase.tariffAmountCents
        )

        private const val FIELD_COD = "cod"
        private const val FIELD_NAME = "name"
        private const val FIELD_COST = "cost"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_SALE_PRICE = "sale_price"
        private const val FIELD_QUANTITY = "quantity"
        private const val FIELD_SALE_DATE = "sale_date"
        private const val FIELD_PAYMENT_METHOD = "payment_method"
        private const val FIELD_TERMINAL_ID = "terminal_id"
        private const val FIELD_TERMINAL_NAME = "terminal_name"
        private const val FIELD_BRAND = "brand"
        private const val FIELD_INSTALLMENTS = "installments"
        private const val FIELD_TARIFF_RATE_BPS = "tariff_rate_bps"
        private const val FIELD_TARIFF_ID = "tariff_id"
        private const val FIELD_TARIFF_AMOUNT_CENTS = "tariff_amount_cents"
    }
}
