package com.cabral.lucrovarejo.ui.payment

import com.cabral.lucrovarejo.domain.payment.PaymentMethods

enum class PaymentMethodsUi(
    val method: PaymentMethods,
    val displayName: String
) {
    CASH(PaymentMethods.CASH, "Dinheiro"),
    PIX(PaymentMethods.PIX, "Pix"),
    CARD(PaymentMethods.CARD, "Cartão")
}
