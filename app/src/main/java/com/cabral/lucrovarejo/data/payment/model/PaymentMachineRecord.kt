package com.cabral.lucrovarejo.data.payment.model

import java.util.Date

data class PaymentMachineRecord(
    val documentId: String,
    val terminalId: String,
    val request: PaymentMachineRequest,
    val createdAt: Date? = null
)
