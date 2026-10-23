package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import java.util.Date
import javax.inject.Inject

/**
 * Resolve a tarifa vigente numa data: entre as tarifas da mesma chave
 * (maquininha + tipo + bandeira + parcela), escolhe a de maior `effectiveFrom <= date`.
 * Função pura e testável, sem dependência de Android/Firebase.
 */
class ResolveFeeForDateUseCase @Inject constructor() {
    operator fun invoke(fees: List<TerminalFeeModel>, date: Date): TerminalFeeModel? =
        fees.filter { !it.effectiveFrom.after(date) }
            .maxByOrNull { it.effectiveFrom }
}
