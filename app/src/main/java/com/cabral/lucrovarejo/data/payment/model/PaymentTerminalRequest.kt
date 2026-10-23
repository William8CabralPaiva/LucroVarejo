package com.cabral.lucrovarejo.data.payment.model

import com.google.firebase.firestore.FieldValue

data class PaymentTerminalRequest(
    val name: String,
    val active: Boolean,
    val enabledMethods: List<String>,
    val enabledBrandIds: List<String>
) {
    fun toFirestoreFields(): Map<String, Any> = toFirestoreUpdateFields() +
        (FIELD_CREATED_AT to FieldValue.serverTimestamp())

    fun toFirestoreUpdateFields(): Map<String, Any> = mapOf(
        FIELD_NAME to name,
        FIELD_ACTIVE to active,
        FIELD_ENABLED_METHODS to enabledMethods,
        FIELD_ENABLED_BRAND_IDS to enabledBrandIds
    )

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_ACTIVE = "active"
        const val FIELD_ENABLED_METHODS = "enabledMethods"
        const val FIELD_ENABLED_BRAND_IDS = "enabledBrandIds"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
