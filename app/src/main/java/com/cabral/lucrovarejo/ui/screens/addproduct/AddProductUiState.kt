package com.cabral.lucrovarejo.ui.screens.addproduct

import androidx.annotation.StringRes
import java.math.BigDecimal
import java.math.RoundingMode

data class AddProductUiState(
    val key: String? = null,
    val name: String = "",
    val cod: String = "",
    val cost: String = "",
    val description: String = "",
    @param:StringRes val nameError: Int? = null,
    @param:StringRes val costError: Int? = null,
    @param:StringRes val errorMessage: Int? = null,
    val isLoading: Boolean = false,
    val showDeleteDialog: Boolean = false
) {
    val isEditing: Boolean
        get() = key != null

    val canSubmit: Boolean
        get() = name.isNotBlank() && parseProductCost(cost) != null && !isLoading
}

internal fun parseProductCost(value: String) = try {
    val amount = BigDecimal(value.trim().replace(',', '.'))
        .setScale(2, RoundingMode.UNNECESSARY)
    amount
        .movePointRight(2)
        .intValueExact()
        .takeIf { it > 0 }
        ?.let { amount }
} catch (_: NumberFormatException) {
    null
} catch (_: ArithmeticException) {
    null
}
