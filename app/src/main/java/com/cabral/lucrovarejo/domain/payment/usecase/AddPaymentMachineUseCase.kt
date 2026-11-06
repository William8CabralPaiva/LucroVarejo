package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.common.DataFailure
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import javax.inject.Inject

class AddPaymentMachineUseCase @Inject constructor(
    private val repository: PaymentMachineRepository,
) {
    suspend operator fun invoke(tariff: PaymentMachineModel): PaymentMachineModel {
        val existingTariffs = repository.getTariffsSnapshot(tariff.terminalId)
        val duplicate = existingTariffs.any { existing ->
            existing.tariffKey() == tariff.tariffKey() && existing.effectiveFrom == tariff.effectiveFrom
        }
        if (duplicate) {
            throw DataFailure(DataFailure.Reason.DUPLICATE_EFFECTIVE_DATE)
        }
        return repository.addTariff(tariff)
    }
}
