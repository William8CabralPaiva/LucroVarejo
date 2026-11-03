package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import javax.inject.Inject

class GetTerminalTariffsUseCase @Inject constructor(
    private val repository: PaymentMachineRepository
) {
    suspend operator fun invoke(terminalId: String): List<PaymentMachineModel> =
        repository.getTariffsSnapshot(terminalId)
}
