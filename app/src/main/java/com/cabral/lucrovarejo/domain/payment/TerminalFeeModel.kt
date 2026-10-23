package com.cabral.lucrovarejo.domain.payment

import java.io.Serializable
import java.util.Date

/**
 * Registro imutável de tarifa (append-only). A chave de vigência é
 * [terminalId] + [method] + [brandId] + [installments]: a tarifa válida numa data D
 * é a de maior [effectiveFrom] <= D para a mesma chave. Nunca editar/apagar um registro;
 * corrigir = inserir um novo com nova [effectiveFrom].
 */
data class TerminalFeeModel(
    val terminalId: String,
    val method: TerminalPaymentType,
    val brandId: String?,
    val installments: Int?,
    val rateBps: Int,
    val effectiveFrom: Date,
    val createdAt: Date? = null,
    val key: String? = null
) : Serializable {
    /** Chave de vigência: tarifas com a mesma chave competem pela mesma data. */
    fun feeKey(): String = listOf(
        terminalId,
        method.name,
        brandId.orEmpty(),
        installments?.toString().orEmpty()
    ).joinToString(separator = "|")
}
