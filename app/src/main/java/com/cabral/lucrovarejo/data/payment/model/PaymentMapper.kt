package com.cabral.lucrovarejo.data.payment.model

import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType

fun CardBrandModel.toRequest(): CardBrandRequest = CardBrandRequest(
    name = name,
    active = active
)

fun CardBrandRecord.toModel(): CardBrandModel = CardBrandModel(
    key = documentId,
    name = request.name,
    active = request.active
)

fun PaymentTerminalModel.toRequest(): PaymentTerminalRequest = PaymentTerminalRequest(
    name = name,
    active = active,
    enabledMethods = enabledMethods.map { it.name },
    enabledBrandIds = enabledBrandIds.toList()
)

fun PaymentTerminalRecord.toModel(): PaymentTerminalModel = PaymentTerminalModel(
    key = documentId,
    name = request.name,
    active = request.active,
    enabledMethods = request.enabledMethods.mapNotNull { methodName ->
        runCatching { TerminalPaymentType.valueOf(methodName) }.getOrNull()
    }.toSet(),
    enabledBrandIds = request.enabledBrandIds.toSet()
)

fun TerminalFeeModel.toRequest(): TerminalFeeRequest = TerminalFeeRequest(
    method = method.name,
    brandId = brandId,
    installments = installments,
    rateBps = rateBps,
    effectiveFrom = effectiveFrom
)

fun TerminalFeeRecord.toModel(): TerminalFeeModel = TerminalFeeModel(
    key = documentId,
    terminalId = terminalId,
    method = TerminalPaymentType.valueOf(request.method),
    brandId = request.brandId,
    installments = request.installments,
    rateBps = request.rateBps,
    effectiveFrom = request.effectiveFrom,
    createdAt = createdAt
)
