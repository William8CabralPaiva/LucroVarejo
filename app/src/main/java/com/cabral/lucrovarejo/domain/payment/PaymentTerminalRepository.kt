package com.cabral.lucrovarejo.domain.payment

import kotlinx.coroutines.flow.Flow

interface PaymentTerminalRepository {
    suspend fun createTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel
    suspend fun updateTerminal(terminal: PaymentTerminalModel): PaymentTerminalModel
    suspend fun archiveTerminal(key: String)
    fun observeTerminals(): Flow<List<PaymentTerminalModel>>
    fun observeTerminal(key: String): Flow<PaymentTerminalModel?>
}
