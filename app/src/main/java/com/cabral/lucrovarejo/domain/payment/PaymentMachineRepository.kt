package com.cabral.lucrovarejo.domain.payment

import kotlinx.coroutines.flow.Flow

interface PaymentMachineRepository {
    /** Insere uma nova tarifa (append-only); rejeita a mesma chave + mesma [PaymentMachineModel.effectiveFrom]. */
    suspend fun addTariff(tariff: PaymentMachineModel): PaymentMachineModel
    fun observeTariffs(terminalId: String): Flow<List<PaymentMachineModel>>
    suspend fun getTariffsSnapshot(terminalId: String): List<PaymentMachineModel>
}
