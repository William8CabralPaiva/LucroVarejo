package com.cabral.lucrovarejo.domain.payment

import java.io.Serializable

data class PaymentTerminalModel(
    val name: String,
    val active: Boolean = true,
    val enabledMethods: Set<TerminalPaymentType> = emptySet(),
    // Bandeiras cadastradas diretamente nesta maquininha (não é um catálogo global: cada
    // maquininha pode aceitar um conjunto diferente de bandeiras).
    val brands: List<String> = emptyList(),
    val key: String? = null
) : Serializable
