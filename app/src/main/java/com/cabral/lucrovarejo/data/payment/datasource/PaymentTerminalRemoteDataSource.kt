package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRecord
import com.cabral.lucrovarejo.data.payment.model.PaymentTerminalRequest
import kotlinx.coroutines.flow.Flow

interface PaymentTerminalRemoteDataSource {
    suspend fun createTerminal(request: PaymentTerminalRequest): String
    suspend fun updateTerminal(key: String, request: PaymentTerminalRequest)
    suspend fun setTerminalActive(key: String, active: Boolean)
    fun observeTerminals(): Flow<List<PaymentTerminalRecord>>
    fun observeTerminal(key: String): Flow<PaymentTerminalRecord?>
}
