package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import javax.inject.Inject

class UpdateTerminalUseCase @Inject constructor(
    private val repository: PaymentTerminalRepository
) {
    suspend operator fun invoke(terminal: PaymentTerminalModel): PaymentTerminalModel =
        repository.updateTerminal(terminal)
}
