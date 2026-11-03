package com.cabral.lucrovarejo.data.payment.model

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType

fun PaymentTerminalModel.toRequest(): PaymentTerminalRequest = PaymentTerminalRequest(
    name = name,
    active = active,
    enabledMethods = enabledMethods.map { it.name },
    brands = brands
)

fun PaymentTerminalRecord.toModel(): PaymentTerminalModel = PaymentTerminalModel(
    key = documentId,
    name = request.name,
    active = request.active,
    enabledMethods = request.enabledMethods.mapNotNull { methodName ->
        runCatching { TerminalPaymentType.valueOf(methodName) }.getOrNull()
    }.toSet(),
    brands = request.brands
)

fun PaymentMachineModel.toRequest(): PaymentMachineRequest = PaymentMachineRequest(
    method = method.name,
    brandId = brandId,
    installments = installments,
    rateBps = rateBps,
    effectiveFrom = effectiveFrom
)

fun PaymentMachineRecord.toModel(): PaymentMachineModel = PaymentMachineModel(
    key = documentId,
    terminalId = terminalId,
    method = TerminalPaymentType.valueOf(request.method),
    brandId = request.brandId,
    installments = request.installments,
    rateBps = request.rateBps,
    effectiveFrom = request.effectiveFrom,
    createdAt = createdAt
)
