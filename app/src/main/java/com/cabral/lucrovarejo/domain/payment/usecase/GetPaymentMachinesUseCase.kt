package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetPaymentMachinesUseCase @Inject constructor(
    private val repository: PaymentMachineRepository
) {
    operator fun invoke(terminalId: String): Flow<List<PaymentMachineModel>> =
        repository.observeTariffs(terminalId)
}
