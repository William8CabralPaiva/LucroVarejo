package com.cabral.lucrovarejo.data.payment.model

import com.google.firebase.firestore.FieldValue

data class CardBrandRequest(
    val name: String,
    val active: Boolean
) {
    fun toFirestoreFields(): Map<String, Any> = mapOf(
        FIELD_NAME to name,
        FIELD_ACTIVE to active,
        FIELD_CREATED_AT to FieldValue.serverTimestamp()
    )

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_ACTIVE = "active"
        const val FIELD_CREATED_AT = "createdAt"
    }
}
