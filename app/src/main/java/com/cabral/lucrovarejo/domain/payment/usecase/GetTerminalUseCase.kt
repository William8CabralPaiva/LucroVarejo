package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTerminalUseCase @Inject constructor(
    private val repository: PaymentTerminalRepository
) {
    operator fun invoke(key: String): Flow<PaymentTerminalModel?> = repository.observeTerminal(key)
}
