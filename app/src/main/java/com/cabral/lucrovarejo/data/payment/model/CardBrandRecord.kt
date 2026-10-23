package com.cabral.lucrovarejo.data.payment.model

import java.util.Date

data class CardBrandRecord(
    val documentId: String,
    val request: CardBrandRequest,
    val createdAt: Date? = null
)
