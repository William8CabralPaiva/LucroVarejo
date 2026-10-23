package com.cabral.lucrovarejo.data.payment.model

import java.util.Date

data class TerminalFeeRecord(
    val documentId: String,
    val terminalId: String,
    val request: TerminalFeeRequest,
    val createdAt: Date? = null
)
