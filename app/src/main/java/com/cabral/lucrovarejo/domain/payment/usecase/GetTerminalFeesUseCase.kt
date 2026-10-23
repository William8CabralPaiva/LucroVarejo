package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalFeeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetTerminalFeesUseCase @Inject constructor(
    private val repository: TerminalFeeRepository
) {
    operator fun invoke(terminalId: String): Flow<List<TerminalFeeModel>> =
        repository.observeFees(terminalId)
}
