package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTerminalsUseCase @Inject constructor(
    private val repository: PaymentTerminalRepository
) {
    operator fun invoke(): Flow<List<PaymentTerminalModel>> = repository.observeTerminals()
}
