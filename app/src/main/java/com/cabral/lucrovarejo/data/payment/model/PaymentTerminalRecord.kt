package com.cabral.lucrovarejo.data.payment.model

import java.util.Date

data class PaymentTerminalRecord(
    val documentId: String,
    val request: PaymentTerminalRequest,
    val createdAt: Date? = null
)
