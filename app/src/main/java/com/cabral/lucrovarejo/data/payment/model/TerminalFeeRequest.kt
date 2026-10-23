package com.cabral.lucrovarejo.data.payment.model

import com.google.firebase.firestore.FieldValue
import java.util.Date

data class TerminalFeeRequest(
    val method: String,
    val brandId: String?,
    val installments: Int?,
    val rateBps: Int,
    val effectiveFrom: Date
) {
    fun toFirestoreFields(): Map<String, Any> = buildMap {
        put(FIELD_METHOD, method)
        brandId?.let { put(FIELD_BRAND_ID, it) }
        installments?.let { put(FIELD_INSTALLMENTS, it) }
        put(FIELD_RATE_BPS, rateBps)
        put(FIELD_EFFECTIVE_FROM, effectiveFrom)
        put(FIELD_CREATED_AT, FieldValue.serverTimestamp())
    }

    companion object {
        const val FIELD_METHOD = "method"
        const val FIELD_BRAND_ID = "brandId"
        const val FIELD_INSTALLMENTS = "installments"
        const val FIELD_RATE_BPS = "rateBps"
        const val FIELD_EFFECTIVE_FROM = "effectiveFrom"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
