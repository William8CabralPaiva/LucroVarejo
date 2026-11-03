package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalRepository
import javax.inject.Inject

class ArchiveTerminalUseCase @Inject constructor(
    private val repository: PaymentTerminalRepository
) {
    suspend operator fun invoke(key: String) = repository.archiveTerminal(key)
}
