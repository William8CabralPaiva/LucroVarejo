package com.cabral.lucrovarejo.ui.screens.machinehistory

import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import java.io.Serializable

/**
 * Identifica um "slot" de tarifa (maquininha + tipo + bandeira opcional + parcela opcional)
 * para abrir a tela de histÃ³rico. Passado via `SavedStateHandle` do back stack, como
 * jÃ¡ Ã© feito com `ProductModel`/`PurchaseModel`.
 */
data class FeeSlotKey(
    val terminalId: String,
    val method: TerminalPaymentType,
    val brandId: String? = null,
    val installments: Int? = null,
) : Serializable
