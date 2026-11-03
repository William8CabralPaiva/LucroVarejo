package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import javax.inject.Inject

class CreateTerminalUseCase @Inject constructor(
    private val repository: PaymentTerminalRepository
) {
    suspend operator fun invoke(terminal: PaymentTerminalModel): PaymentTerminalModel =
        repository.createTerminal(terminal)
}
