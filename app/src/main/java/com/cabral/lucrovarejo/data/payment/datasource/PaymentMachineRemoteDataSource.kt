package com.cabral.lucrovarejo.data.payment.datasource

import com.cabral.lucrovarejo.data.payment.model.PaymentMachineRecord
import com.cabral.lucrovarejo.data.payment.model.PaymentMachineRequest
import kotlinx.coroutines.flow.Flow

interface PaymentMachineRemoteDataSource {
    suspend fun addTariff(terminalId: String, request: PaymentMachineRequest): String
    fun observeTariffs(terminalId: String): Flow<List<PaymentMachineRecord>>
    suspend fun getTariffsSnapshot(terminalId: String): List<PaymentMachineRecord>
}
