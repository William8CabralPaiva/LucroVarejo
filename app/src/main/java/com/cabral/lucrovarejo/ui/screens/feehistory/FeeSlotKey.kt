package com.cabral.lucrovarejo.ui.screens.feehistory

import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import java.io.Serializable

/**
 * Identifica um "slot" de tarifa (maquininha + tipo + bandeira opcional + parcela opcional)
 * para abrir a tela de histórico. Passado via `SavedStateHandle` do back stack, como
 * já é feito com `ProductModel`/`PurchaseModel`.
 */
data class FeeSlotKey(
    val terminalId: String,
    val method: TerminalPaymentType,
    val brandId: String? = null,
    val installments: Int? = null,
) : Serializable
