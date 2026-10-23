package com.cabral.lucrovarejo.domain.payment

import kotlinx.coroutines.flow.Flow

interface TerminalFeeRepository {
    /** Insere uma nova tarifa (append-only); rejeita a mesma chave + mesma [TerminalFeeModel.effectiveFrom]. */
    suspend fun addFee(fee: TerminalFeeModel): TerminalFeeModel
    fun observeFees(terminalId: String): Flow<List<TerminalFeeModel>>
    suspend fun getFeesSnapshot(terminalId: String): List<TerminalFeeModel>
}
