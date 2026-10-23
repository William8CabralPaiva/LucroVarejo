package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentFailure
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalFeeRepository
import javax.inject.Inject

class AddTerminalFeeUseCase @Inject constructor(
    private val repository: TerminalFeeRepository,
) {
    suspend operator fun invoke(fee: TerminalFeeModel): TerminalFeeModel {
        val existingFees = repository.getFeesSnapshot(fee.terminalId)
        val duplicate = existingFees.any { existing ->
            existing.feeKey() == fee.feeKey() && existing.effectiveFrom == fee.effectiveFrom
        }
        if (duplicate) {
            throw PaymentFailure(PaymentFailure.Reason.DUPLICATE_EFFECTIVE_DATE)
        }
        return repository.addFee(fee)
    }
}
