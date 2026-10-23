package com.cabral.lucrovarejo.domain.payment

import java.io.Serializable

data class PaymentTerminalModel(
    val name: String,
    val active: Boolean = true,
    val enabledMethods: Set<TerminalPaymentType> = emptySet(),
    val enabledBrandIds: Set<String> = emptySet(),
    val key: String? = null
) : Serializable
