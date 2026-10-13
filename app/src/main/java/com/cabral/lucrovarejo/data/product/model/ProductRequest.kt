package com.cabral.lucrovarejo.data.product.model

data class ProductRequest(
    val cod: String,
    val name: String,
    val cost: Int,
    val description: String
) {
    fun toFirestoreFields(): Map<String, Any> = mapOf(
        FIELD_COD to cod,
        FIELD_NAME to name,
        FIELD_COST to cost,
        FIELD_DESCRIPTION to description
    )

    private companion object {
        const val FIELD_COD = "cod"
        const val FIELD_NAME = "name"
        const val FIELD_COST = "cost"
        const val FIELD_DESCRIPTION = "description"
    }
}
