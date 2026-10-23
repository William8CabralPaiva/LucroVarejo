package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.TerminalFeeRecord
import com.cabral.lucrovarejo.data.payment.model.TerminalFeeRequest
import kotlinx.coroutines.flow.Flow

interface TerminalFeeRemoteDataSource {
    suspend fun addFee(terminalId: String, request: TerminalFeeRequest): String
    fun observeFees(terminalId: String): Flow<List<TerminalFeeRecord>>
    suspend fun getFeesSnapshot(terminalId: String): List<TerminalFeeRecord>
}
